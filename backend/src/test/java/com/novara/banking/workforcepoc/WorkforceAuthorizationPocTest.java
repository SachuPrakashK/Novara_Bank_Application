package com.novara.banking.workforcepoc;

import static com.novara.banking.workforcepoc.WorkforceAuthorizationPoc.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkforceAuthorizationPocTest {
    private final Instant now = Instant.parse("2026-10-26T12:00:00Z");
    private final UUID staffId = UUID.randomUUID();
    private IdentityResolver identities;
    private EntitlementStore entitlements;
    private AuthorizationService authorization;

    @BeforeEach void setup() {
        identities = new IdentityResolver();
        identities.register(new Staff(staffId, new ExternalIdentity(WORKFORCE_ISSUER, "staff-subject-a"), true));
        entitlements = new EntitlementStore();
        authorization = new AuthorizationService(identities, entitlements);
    }

    @Test void activeScopedEntitlementWithRecentMfaAllowsSyntheticResource() {
        entitlements.grant(staffId, "SERVICE_REQUEST_READ", "ORG-A", "QUEUE-A", now.minusSeconds(60), now.plusSeconds(600), "POC-APPROVAL-1");
        assertThat(authorization.evaluate(validToken(), request("ORG-A", "QUEUE-A"))).isEqualTo(new Decision(true, "ALLOWED"));
    }

    @Test void customerIssuerCannotResolveAsWorkforce() { denied(token("http://localhost:8081/realms/novara", true, true, now.plusSeconds(60), now), "WRONG_ISSUER"); }
    @Test void wrongAudienceIsDenied() { denied(new TokenEvidence(WORKFORCE_ISSUER,"staff-subject-a",Set.of("novara-api"),WORKFORCE_CLIENT,now.plusSeconds(60),now,true,true), "WRONG_AUDIENCE"); }
    @Test void wrongClientIsDenied() { denied(new TokenEvidence(WORKFORCE_ISSUER,"staff-subject-a",Set.of(WORKFORCE_AUDIENCE),"customer-client",now.plusSeconds(60),now,true,true), "WRONG_CLIENT"); }
    @Test void invalidSignatureIsDenied() { denied(token(WORKFORCE_ISSUER, false, true, now.plusSeconds(60), now), "INVALID_SIGNATURE"); }
    @Test void expiredTokenIsDenied() { denied(token(WORKFORCE_ISSUER, true, true, now.minusSeconds(1), now.minusSeconds(60)), "TOKEN_EXPIRED"); }
    @Test void missingMfaIsDenied() { denied(token(WORKFORCE_ISSUER, true, false, now.plusSeconds(60), now), "MFA_REQUIRED"); }
    @Test void staleMfaIsDenied() { denied(token(WORKFORCE_ISSUER, true, true, now.plusSeconds(60), now.minusSeconds(301)), "MFA_STALE"); }
    @Test void suspendedStaffIsDenied() {
        identities.register(new Staff(staffId, new ExternalIdentity(WORKFORCE_ISSUER, "staff-subject-a"), false));
        denied(validToken(), "STAFF_SUSPENDED");
    }

    @Test void expiredAndFutureAssignmentsAreDenied() {
        entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now.minusSeconds(600),now,"POC-2");
        entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now.plusSeconds(1),now.plusSeconds(600),"POC-3");
        denied(validToken(), "NO_ACTIVE_SCOPED_ENTITLEMENT");
    }

    @Test void revocationTakesEffectWithoutCache() {
        Entitlement value=entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now.minusSeconds(1),now.plusSeconds(60),"POC-4");
        assertThat(authorization.evaluate(validToken(),request("ORG-A","QUEUE-A")).allowed()).isTrue();
        entitlements.revoke(value.id(),0);
        denied(validToken(),"NO_ACTIVE_SCOPED_ENTITLEMENT");
    }

    @Test void scopeReductionAndCrossOrganizationAccessAreDenied() {
        Entitlement value=entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now.minusSeconds(1),now.plusSeconds(60),"POC-5");
        entitlements.reduceScope(value.id(),0,"QUEUE-B");
        denied(validToken(),"NO_ACTIVE_SCOPED_ENTITLEMENT");
        assertThat(authorization.evaluate(validToken(),request("ORG-A","QUEUE-B")).allowed()).isTrue();
        assertThat(authorization.evaluate(validToken(),request("ORG-B","QUEUE-B")).allowed()).isFalse();
    }

    @Test void staleConcurrentEntitlementUpdateFails() {
        Entitlement value=entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now.minusSeconds(1),now.plusSeconds(60),"POC-6");
        entitlements.reduceScope(value.id(),0,"QUEUE-B");
        assertThatThrownBy(()->entitlements.revoke(value.id(),0)).isInstanceOf(Denied.class).hasMessage("STALE_ASSIGNMENT_VERSION");
    }

    @Test void invalidAssignmentAndMissingPurposeAreDenied() {
        assertThatThrownBy(()->entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now,now,"POC-7")).hasMessage("INVALID_ASSIGNMENT");
        entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now.minusSeconds(1),now.plusSeconds(60),"POC-8");
        Decision result=authorization.evaluate(validToken(),new Request("SERVICE_REQUEST_READ","ORG-A","QUEUE-A","",now));
        assertThat(result.reason()).isEqualTo("PURPOSE_REQUIRED");
    }

    @Test void securityAuditUsesOpaqueReferencesAndNoTokenMaterial() {
        entitlements.grant(staffId,"SERVICE_REQUEST_READ","ORG-A","QUEUE-A",now.minusSeconds(1),now.plusSeconds(60),"POC-9");
        authorization.evaluate(validToken(),request("ORG-A","QUEUE-A"));
        assertThat(entitlements.events()).allMatch(event->event.actorReference().startsWith("staff-") && !event.actorReference().contains("staff-subject-a"));
        assertThat(authorization.events()).allMatch(event->!event.actorReference().contains("staff-subject-a"));
    }

    @Test void makerCannotApproveOwnCase() {
        ApprovalService service=new ApprovalService(); ApprovalCase value=service.create(staffId,"ORG-A",now.plusSeconds(60));
        approvalDenied(service,value,staffId,"ORG-A",true,now,0,"SELF_APPROVAL_DENIED");
    }

    @Test void revokedCheckerAndCrossScopeCheckerCannotApprove() {
        ApprovalService service=new ApprovalService(); ApprovalCase value=service.create(staffId,"ORG-A",now.plusSeconds(60)); UUID checker=UUID.randomUUID();
        approvalDenied(service,value,checker,"ORG-A",false,now,0,"CHECKER_NOT_ENTITLED");
        approvalDenied(service,value,checker,"ORG-B",true,now,0,"CROSS_SCOPE_APPROVAL_DENIED");
    }

    @Test void expiredStaleAndDuplicateApprovalsFailDeterministically() {
        ApprovalService service=new ApprovalService(); UUID checker=UUID.randomUUID();
        ApprovalCase expired=service.create(staffId,"ORG-A",now.minusSeconds(1));
        approvalDenied(service,expired,checker,"ORG-A",true,now,0,"APPROVAL_EXPIRED");
        ApprovalCase active=service.create(staffId,"ORG-A",now.plusSeconds(60));
        ApprovalCase approved=service.approve(active.id(),checker,"ORG-A",true,now,0);
        approvalDenied(service,approved,UUID.randomUUID(),"ORG-A",true,now,0,"STALE_APPROVAL_VERSION");
        approvalDenied(service,approved,checker,"ORG-A",true,now,approved.version(),"DUPLICATE_APPROVAL");
    }

    private TokenEvidence validToken(){return token(WORKFORCE_ISSUER,true,true,now.plusSeconds(60),now.minusSeconds(60));}
    private TokenEvidence token(String issuer,boolean signature,boolean mfa,Instant expiry,Instant authentication){return new TokenEvidence(issuer,"staff-subject-a",Set.of(WORKFORCE_AUDIENCE),WORKFORCE_CLIENT,expiry,authentication,signature,mfa);}
    private Request request(String organization,String scope){return new Request("SERVICE_REQUEST_READ",organization,scope,"SYNTHETIC_CASE_REVIEW",now);}
    private void denied(TokenEvidence token,String reason){assertThat(authorization.evaluate(token,request("ORG-A","QUEUE-A"))).isEqualTo(new Decision(false,reason));}
    private void approvalDenied(ApprovalService service,ApprovalCase value,UUID checker,String org,boolean entitled,Instant at,long version,String reason){assertThatThrownBy(()->service.approve(value.id(),checker,org,entitled,at,version)).isInstanceOf(Denied.class).hasMessage(reason);}
}
