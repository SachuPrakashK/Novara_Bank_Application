package com.novara.banking.account;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.novara.banking.identity.CurrentIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Validated @RestController @RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountRepository accounts;
    private final CurrentIdentity identities;
    public AccountController(AccountRepository accounts, CurrentIdentity identities) { this.accounts = accounts; this.identities = identities; }
    @GetMapping List<AccountView> list(Authentication auth) { return accounts.findOwned(identities.subject(auth)); }
    @GetMapping("/{id}") AccountView detail(Authentication auth,@PathVariable UUID id) { return accounts.requireOwned(identities.subject(auth),id); }
    @GetMapping("/{id}/balance") AccountView balance(Authentication auth,@PathVariable UUID id) { return accounts.requireOwned(identities.subject(auth),id); }
    @GetMapping("/{id}/transactions") List<AccountTransactionView> transactions(Authentication auth,@PathVariable UUID id,@RequestParam(defaultValue="50") @Min(1) @Max(200) int limit) { return accounts.transactions(identities.subject(auth),id,limit); }
}
