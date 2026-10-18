package com.novara.banking.customer;

import com.novara.banking.account.AccountRepository;
import com.novara.banking.account.AccountView;
import com.novara.banking.account.CustomerTransactionView;
import com.novara.banking.identity.CurrentIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/me")
public class DashboardController {
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final CurrentIdentity identities;
    public DashboardController(CustomerRepository customers, AccountRepository accounts, CurrentIdentity identities) { this.customers = customers; this.accounts = accounts; this.identities = identities; }

    @GetMapping("/dashboard")
    Dashboard dashboard(Authentication auth) {
        var identity = identities.from(auth);
        var customer = customers.requireByIdentity(identity.issuer(), identity.subject());
        var owned = accounts.findOwned(identity.subject());
        var transactions = accounts.transactions(identity.subject(), 20);
        var displayName = customer.displayName().trim();
        var firstName = displayName.isEmpty() ? "Customer" : displayName.split("\\s+")[0];
        var initials = displayName.isEmpty() ? "NB" : displayName.codePoints().filter(Character::isLetter).limit(2).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString().toUpperCase();
        long total = owned.stream().mapToLong(AccountView::ledgerBalanceMinor).sum();
        long spend = transactions.stream().filter(t -> "DEBIT".equals(t.direction())).mapToLong(t -> t.amount().amountMinor()).sum();
        return new Dashboard(new Profile(customer.id(),firstName,initials,"Customer"),new Money(total,"INR"),owned,transactions,new Money(spend,"INR"),new Security("OIDC protected",false,"Managed by identity provider"));
    }
    record Dashboard(Profile customer, Money relationshipBalance, List<AccountView> accounts, List<CustomerTransactionView> transactions, Money monthlySpend, Security security) {}
    record Profile(UUID id, String firstName, String initials, String tier) {}
    record Money(long amountMinor, String currency) {}
    record Security(String label, boolean mfaEnabled, String lastLogin) {}
}
