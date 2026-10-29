package com.novara.banking.workforcepoc;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class WorkforceAuthorizationPoc {
    static final String WORKFORCE_ISSUER = "http://localhost:8082/realms/novara-workforce-test";
    static final String WORKFORCE_AUDIENCE = "novara-workforce-poc-api";
    static final String WORKFORCE_CLIENT = "novara-workforce-poc";
    static final Duration MFA_MAX_AGE = Duration.ofMinutes(5);

    record ExternalIdentity(String issuer, String subject) {}
    record Staff(UUID id, ExternalIdentity externalIdentity, boolean active) {}
    record TokenEvidence(String issuer, String subject, Set<String> audience, String clientId,
                         Instant expiresAt, Instant authenticatedAt, boolean signatureVerified,
                         boolean mfaVerified) {}
    record Entitlement(UUID id, UUID staffId, String permission, String organization,
                       String resourceScope, Instant validFrom, Instant validUntil,
                       boolean revoked, String approvalReference, long version) {}
    record Request(String permission, String organization, String resourceScope,
                   String businessPurpose, Instant now) {}
    record Decision(boolean allowed, String reason) {}
    record SecurityEvent(String action, String outcome, String actorReference, String reason) {}

    static final class IdentityResolver {
        private final Map<ExternalIdentity, Staff> staff = new HashMap<>();

        void register(Staff value) { staff.put(value.externalIdentity(), value); }

        Staff resolve(TokenEvidence token, Instant now) {
            require(token.signatureVerified(), "INVALID_SIGNATURE");
            require(WORKFORCE_ISSUER.equals(token.issuer()), "WRONG_ISSUER");
            require(token.audience().contains(WORKFORCE_AUDIENCE), "WRONG_AUDIENCE");
            require(WORKFORCE_CLIENT.equals(token.clientId()), "WRONG_CLIENT");
            require(token.expiresAt().isAfter(now), "TOKEN_EXPIRED");
            Staff result = staff.get(new ExternalIdentity(token.issuer(), token.subject()));
            require(result != null, "STAFF_IDENTITY_NOT_MAPPED");
            require(result.active(), "STAFF_SUSPENDED");
            return result;
        }
    }

    static final class EntitlementStore {
        private final Map<UUID, Entitlement> values = new HashMap<>();
        private final List<SecurityEvent> events = new ArrayList<>();

        synchronized Entitlement grant(UUID staffId, String permission, String organization,
                                       String resourceScope, Instant from, Instant until,
                                       String approvalReference) {
            require(until.isAfter(from), "INVALID_ASSIGNMENT");
            require(approvalReference != null && !approvalReference.isBlank(), "APPROVAL_REQUIRED");
            Entitlement value = new Entitlement(UUID.randomUUID(), staffId, permission, organization,
                resourceScope, from, until, false, approvalReference, 0);
            values.put(value.id(), value);
            events.add(new SecurityEvent("ENTITLEMENT_GRANTED", "SUCCESS", opaque(staffId), "APPROVED_REFERENCE"));
            return value;
        }

        synchronized Entitlement revoke(UUID id, long expectedVersion) {
            Entitlement current = requireVersion(id, expectedVersion);
            Entitlement updated = new Entitlement(current.id(), current.staffId(), current.permission(),
                current.organization(), current.resourceScope(), current.validFrom(), current.validUntil(),
                true, current.approvalReference(), current.version() + 1);
            values.put(id, updated);
            events.add(new SecurityEvent("ENTITLEMENT_REVOKED", "SUCCESS", opaque(current.staffId()), "REVOKED"));
            return updated;
        }

        synchronized Entitlement reduceScope(UUID id, long expectedVersion, String resourceScope) {
            Entitlement current = requireVersion(id, expectedVersion);
            Entitlement updated = new Entitlement(current.id(), current.staffId(), current.permission(),
                current.organization(), resourceScope, current.validFrom(), current.validUntil(), current.revoked(),
                current.approvalReference(), current.version() + 1);
            values.put(id, updated);
            return updated;
        }

        synchronized List<Entitlement> forStaff(UUID staffId) {
            return values.values().stream().filter(value -> value.staffId().equals(staffId)).toList();
        }

        List<SecurityEvent> events() { return List.copyOf(events); }

        private Entitlement requireVersion(UUID id, long expectedVersion) {
            Entitlement current = values.get(id);
            require(current != null, "ASSIGNMENT_NOT_FOUND");
            require(current.version() == expectedVersion, "STALE_ASSIGNMENT_VERSION");
            return current;
        }
    }

    static final class AuthorizationService {
        private final IdentityResolver identities;
        private final EntitlementStore entitlements;
        private final List<SecurityEvent> events = new ArrayList<>();

        AuthorizationService(IdentityResolver identities, EntitlementStore entitlements) {
            this.identities = identities;
            this.entitlements = entitlements;
        }

        Decision evaluate(TokenEvidence token, Request request) {
            try {
                Staff staff = identities.resolve(token, request.now());
                require(token.mfaVerified(), "MFA_REQUIRED");
                require(!token.authenticatedAt().isAfter(request.now()) &&
                    Duration.between(token.authenticatedAt(), request.now()).compareTo(MFA_MAX_AGE) <= 0,
                    "MFA_STALE");
                require(request.businessPurpose() != null && !request.businessPurpose().isBlank(), "PURPOSE_REQUIRED");
                boolean allowed = entitlements.forStaff(staff.id()).stream().anyMatch(value ->
                    !value.revoked() && !request.now().isBefore(value.validFrom()) && request.now().isBefore(value.validUntil()) &&
                    value.permission().equals(request.permission()) && value.organization().equals(request.organization()) &&
                    value.resourceScope().equals(request.resourceScope()));
                require(allowed, "NO_ACTIVE_SCOPED_ENTITLEMENT");
                events.add(new SecurityEvent("AUTHORIZATION_EVALUATED", "ALLOWED", opaque(staff.id()), "POLICY_MATCH"));
                return new Decision(true, "ALLOWED");
            } catch (Denied denied) {
                events.add(new SecurityEvent("AUTHORIZATION_EVALUATED", "DENIED", "unresolved-or-masked", denied.getMessage()));
                return new Decision(false, denied.getMessage());
            }
        }

        List<SecurityEvent> events() { return List.copyOf(events); }
    }

    record ApprovalCase(UUID id, UUID maker, String organization, Instant expiresAt, long version, Set<UUID> checkers) {}

    static final class ApprovalService {
        private final Map<UUID, ApprovalCase> cases = new HashMap<>();

        ApprovalCase create(UUID maker, String organization, Instant expiresAt) {
            ApprovalCase value = new ApprovalCase(UUID.randomUUID(), maker, organization, expiresAt, 0, new HashSet<>());
            cases.put(value.id(), value);
            return value;
        }

        synchronized ApprovalCase approve(UUID id, UUID checker, String checkerOrganization,
                                          boolean checkerEntitled, Instant now, long expectedVersion) {
            ApprovalCase current = cases.get(id);
            require(current != null, "APPROVAL_NOT_FOUND");
            require(current.version() == expectedVersion, "STALE_APPROVAL_VERSION");
            require(!current.maker().equals(checker), "SELF_APPROVAL_DENIED");
            require(checkerEntitled, "CHECKER_NOT_ENTITLED");
            require(current.organization().equals(checkerOrganization), "CROSS_SCOPE_APPROVAL_DENIED");
            require(current.expiresAt().isAfter(now), "APPROVAL_EXPIRED");
            require(!current.checkers().contains(checker), "DUPLICATE_APPROVAL");
            Set<UUID> updatedCheckers = new HashSet<>(current.checkers());
            updatedCheckers.add(checker);
            ApprovalCase updated = new ApprovalCase(current.id(), current.maker(), current.organization(),
                current.expiresAt(), current.version() + 1, Set.copyOf(updatedCheckers));
            cases.put(id, updated);
            return updated;
        }
    }

    private static String opaque(UUID id) { return "staff-" + id.toString().substring(0, 8); }
    private static void require(boolean condition, String reason) { if (!condition) throw new Denied(reason); }
    static final class Denied extends RuntimeException { Denied(String reason) { super(reason); } }
}
