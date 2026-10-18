package com.novara.banking.account;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.novara.banking.identity.CurrentIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final AccountRepository accounts;
    private final CurrentIdentity identities;
    public TransactionController(AccountRepository accounts, CurrentIdentity identities) { this.accounts = accounts; this.identities = identities; }
    @GetMapping List<CustomerTransactionView> list(Authentication auth, @RequestParam(defaultValue="100") @Min(1) @Max(200) int limit) {
        return accounts.transactions(identities.subject(auth), limit);
    }
}
