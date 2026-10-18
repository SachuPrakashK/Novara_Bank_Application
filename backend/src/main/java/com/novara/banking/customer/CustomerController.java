package com.novara.banking.customer;

import com.novara.banking.identity.CurrentIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/api/v1")
public class CustomerController {
    private final CustomerRepository customers;
    private final CurrentIdentity identities;
    public CustomerController(CustomerRepository customers, CurrentIdentity identities) { this.customers = customers; this.identities = identities; }
    @GetMapping("/me") CustomerProfile me(Authentication authentication) {
        var identity = identities.from(authentication);
        return customers.requireByIdentity(identity.issuer(), identity.subject());
    }
}
