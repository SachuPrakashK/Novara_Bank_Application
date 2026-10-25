package com.novara.banking.deposit;

import com.novara.banking.deposit.DepositModels.*;
import com.novara.banking.identity.*;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/deposits")
public class DepositController {
    private final DepositService deposits; private final CurrentIdentity identities; private final StepUpAuthorizationService stepUp;
    public DepositController(DepositService deposits,CurrentIdentity identities,StepUpAuthorizationService stepUp){this.deposits=deposits;this.identities=identities;this.stepUp=stepUp;}
    @GetMapping("/products") List<Product> products(@RequestParam(required=false) String type){return deposits.products(type);}
    @GetMapping List<Deposit> list(Authentication auth){return deposits.list(identities.from(auth));}
    @GetMapping("/{id}") Deposit detail(Authentication auth,@PathVariable UUID id){return deposits.requireOwned(identities.from(auth),id);}
    @GetMapping("/{id}/schedule") List<ScheduleItem> schedule(Authentication auth,@PathVariable UUID id){return deposits.schedule(identities.from(auth),id);}
    @PostMapping("/fixed/reviews") Review fixedReview(Authentication auth,@Valid @RequestBody ReviewRequest request){return deposits.review(identities.from(auth),request,"FIXED");}
    @PostMapping("/recurring/reviews") Review recurringReview(Authentication auth,@Valid @RequestBody ReviewRequest request){return deposits.review(identities.from(auth),request,"RECURRING");}
    @PostMapping("/fixed/confirmations") Deposit fixedConfirm(Authentication auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody ReviewRequest request){stepUp.requireForSensitiveAction(auth,"Recent MFA is required to open a fixed deposit.");return deposits.confirm(identities.from(auth),request,"FIXED",key,MDC.get("correlationId"));}
    @PostMapping("/recurring/confirmations") Deposit recurringConfirm(Authentication auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody ReviewRequest request){stepUp.requireForSensitiveAction(auth,"Recent MFA is required to open a recurring deposit.");return deposits.confirm(identities.from(auth),request,"RECURRING",key,MDC.get("correlationId"));}
}
