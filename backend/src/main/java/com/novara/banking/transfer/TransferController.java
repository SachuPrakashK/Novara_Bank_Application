package com.novara.banking.transfer;

import com.novara.banking.shared.CorrelationIdFilter;
import com.novara.banking.shared.DomainException;
import com.novara.banking.audit.AuditService;
import com.novara.banking.identity.CurrentIdentity;
import com.novara.banking.identity.StepUpAuthorizationService;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/transfers")
public class TransferController {
    private final InternalTransferService transfers; private final AuditService audit; private final CurrentIdentity identities; private final StepUpAuthorizationService stepUp;
    public TransferController(InternalTransferService transfers,AuditService audit,CurrentIdentity identities,StepUpAuthorizationService stepUp){this.transfers=transfers;this.audit=audit;this.identities=identities;this.stepUp=stepUp;}
    @PostMapping("/internal") @ResponseStatus(HttpStatus.CREATED)
    TransferView create(Authentication auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody InternalTransferRequest request){
        String subject=identities.subject(auth);
        try{stepUp.requireForTransfer(request.amountMinor(),auth);return transfers.execute(subject,key,request,MDC.get("correlationId"));}
        catch(DomainException exception){audit.recordFailure(subject,"INTERNAL_TRANSFER","TRANSFER",MDC.get("correlationId"),exception.code());throw exception;}
    }
    @GetMapping("/{id}") TransferView get(Authentication auth,@PathVariable UUID id){return transfers.requireOwned(identities.subject(auth),id);}
    @GetMapping("/by-idempotency-key/{key}") TransferView byKey(Authentication auth,@PathVariable String key){return transfers.requireByIdempotency(identities.subject(auth),key);}
}
