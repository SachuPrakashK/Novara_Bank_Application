package com.novara.banking.beneficiary;

import com.novara.banking.audit.AuditService;
import com.novara.banking.identity.CurrentIdentity;
import com.novara.banking.identity.StepUpAuthorizationService;
import com.novara.banking.shared.DomainException;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {
    private final BeneficiaryService beneficiaries;
    private final CurrentIdentity identities;
    private final StepUpAuthorizationService stepUp;
    private final AuditService audit;
    public BeneficiaryController(BeneficiaryService beneficiaries, CurrentIdentity identities, StepUpAuthorizationService stepUp, AuditService audit) {
        this.beneficiaries=beneficiaries; this.identities=identities; this.stepUp=stepUp; this.audit=audit;
    }
    @GetMapping List<BeneficiaryView> list(Authentication auth, @RequestParam(required=false) String query, @RequestParam(required=false) String status, @RequestParam(required=false) Boolean favorite) {
        var identity=validated(auth); return beneficiaries.list(identity.issuer(),identity.subject(),query,status,favorite);
    }
    @GetMapping("/{id}") BeneficiaryView get(Authentication auth,@PathVariable UUID id) { var identity=validated(auth); return beneficiaries.requireOwned(identity.issuer(),identity.subject(),id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) BeneficiaryView create(Authentication auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody CreateBeneficiaryRequest request) {
        var identity=validated(auth); return beneficiaries.create(identity.issuer(),identity.subject(),key,request,MDC.get("correlationId"));
    }
    @PatchMapping("/{id}") BeneficiaryView update(Authentication auth,@PathVariable UUID id,@Valid @RequestBody UpdateBeneficiaryRequest request) {
        var identity=validated(auth); return beneficiaries.updateProfile(identity.issuer(),identity.subject(),id,request,MDC.get("correlationId"));
    }
    @PutMapping("/{id}/favorite") BeneficiaryView favorite(Authentication auth,@PathVariable UUID id,@Valid @RequestBody FavoriteBeneficiaryRequest request) {
        var identity=validated(auth); return beneficiaries.setFavorite(identity.issuer(),identity.subject(),id,request.favorite(),MDC.get("correlationId"));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void deactivate(Authentication auth,@PathVariable UUID id) {
        var identity=validated(auth);
        try { stepUp.requireForSensitiveAction(auth,"Recent identity-provider MFA is required to deactivate a beneficiary."); beneficiaries.deactivate(identity.issuer(),identity.subject(),id,MDC.get("correlationId")); }
        catch (DomainException exception) { audit.recordFailure(identity.subject(),"BENEFICIARY_DEACTIVATE","BENEFICIARY",MDC.get("correlationId"),exception.code()); throw exception; }
    }
    private CurrentIdentity.FederatedIdentity validated(Authentication auth) { identities.subject(auth); return identities.from(auth); }
}

