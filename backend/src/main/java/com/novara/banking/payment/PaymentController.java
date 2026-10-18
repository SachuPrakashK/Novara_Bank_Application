package com.novara.banking.payment;

import com.novara.banking.identity.CurrentIdentity;
import com.novara.banking.identity.StepUpAuthorizationService;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService payments; private final PaymentScheduleService schedules; private final CurrentIdentity identities; private final StepUpAuthorizationService stepUp;
    public PaymentController(PaymentService payments,PaymentScheduleService schedules,CurrentIdentity identities,StepUpAuthorizationService stepUp){this.payments=payments;this.schedules=schedules;this.identities=identities;this.stepUp=stepUp;}

    @PostMapping("/payments/intents") @ResponseStatus(HttpStatus.CREATED)
    PaymentView intent(Authentication auth,@Valid @RequestBody CreatePaymentIntentRequest request){return payments.createIntent(identities.from(auth),request,MDC.get("correlationId"));}
    @PostMapping("/payments/{id}/confirm")
    PaymentView confirm(Authentication auth,@PathVariable UUID id,@RequestHeader("Idempotency-Key") String key){var identity=identities.from(auth);var payment=payments.requireOwned(identity,id);if(payment.requiresStepUp())stepUp.requireForSensitiveAction(auth,"Recent identity-provider MFA is required for this payment.");return payments.confirm(identity,id,key,MDC.get("correlationId"));}
    @GetMapping("/payments/{id}") PaymentView get(Authentication auth,@PathVariable UUID id){return payments.requireOwned(identities.from(auth),id);}
    @GetMapping("/payments") PaymentPage list(Authentication auth,@RequestParam(required=false) String status,@RequestParam(required=false) String type,@RequestParam(required=false) String query,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return payments.list(identities.from(auth),status,type,query,page,size);}
    @GetMapping("/payment-limits") PaymentLimitView limits(Authentication auth){return payments.limits(identities.from(auth));}
    @PatchMapping("/payment-limits/customer-preferences") PaymentLimitView lowerLimit(Authentication auth,@Valid @RequestBody UpdateCustomerLimitRequest request){return payments.lowerCustomerLimit(identities.from(auth),request.dailyLimitMinor(),MDC.get("correlationId"));}

    @GetMapping("/payment-schedules") java.util.List<PaymentScheduleView> schedules(Authentication auth){return schedules.list(identities.from(auth));}
    @PostMapping("/payment-schedules") @ResponseStatus(HttpStatus.CREATED) PaymentScheduleView schedule(Authentication auth,@Valid @RequestBody CreatePaymentScheduleRequest request){return schedules.create(identities.from(auth),request,MDC.get("correlationId"));}
    @GetMapping("/payment-schedules/{id}") PaymentScheduleView schedule(Authentication auth,@PathVariable UUID id){return schedules.requireOwned(identities.from(auth),id);}
    @PostMapping("/payment-schedules/{id}/cancel") PaymentScheduleView cancel(Authentication auth,@PathVariable UUID id){return schedules.cancel(identities.from(auth),id,MDC.get("correlationId"));}

    @GetMapping(value="/payments/{id}/receipt",produces=MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentView> receipt(Authentication auth,@PathVariable UUID id){var payment=payments.requireOwned(identities.from(auth),id);if(!payment.status().equals("COMPLETED"))throw new com.novara.banking.shared.DomainException(HttpStatus.CONFLICT,"RECEIPT_NOT_AVAILABLE","A receipt is available only after completed posting.");return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(payment);}
}
