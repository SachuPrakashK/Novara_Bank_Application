package com.novara.banking.loan;
import com.novara.banking.identity.*;import com.novara.banking.loan.LoanModels.*;import jakarta.validation.Valid;import org.slf4j.MDC;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/v1/loans") public class LoanController {
 private final LoanService loans;private final CurrentIdentity identities;private final StepUpAuthorizationService stepUp;
 public LoanController(LoanService loans,CurrentIdentity identities,StepUpAuthorizationService stepUp){this.loans=loans;this.identities=identities;this.stepUp=stepUp;}
 @GetMapping("/products") List<Product> products(){return loans.products();}
 @PostMapping("/quotes") Quote quote(Authentication auth,@Valid @RequestBody QuoteRequest request){return loans.quote(identities.from(auth),request);}
 @PostMapping("/applications") Application apply(Authentication auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody ApplicationRequest request){stepUp.requireForSensitiveAction(auth,"Recent MFA is required to submit a sandbox loan application.");return loans.apply(identities.from(auth),request,key,MDC.get("correlationId"));}
 @GetMapping("/applications") List<Application> applications(Authentication auth){return loans.applications(identities.from(auth));}
 @GetMapping("/applications/{id}") Application application(Authentication auth,@PathVariable UUID id){return loans.requireApplication(identities.from(auth),id);}
 @GetMapping("/quotes/{id}/schedule") List<Installment> schedule(Authentication auth,@PathVariable UUID id){return loans.quoteSchedule(identities.from(auth),id);}
}
