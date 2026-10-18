package com.novara.banking.identity;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import com.novara.banking.customer.CustomerRepository;

@Component
public class CurrentIdentity {
    private final String defaultIssuer;
    private final CustomerRepository customers;
    public CurrentIdentity(@Value("${novara.security.issuer}") String defaultIssuer, CustomerRepository customers) { this.defaultIssuer = defaultIssuer; this.customers = customers; }
    public FederatedIdentity from(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) return new FederatedIdentity(jwt.getIssuer() == null ? defaultIssuer : jwt.getIssuer().toString(), jwt.getSubject());
        if (principal instanceof OidcUser oidc) return new FederatedIdentity(oidc.getIssuer().toString(), oidc.getSubject());
        throw new IllegalStateException("Unsupported authenticated principal");
    }
    public String subject(Authentication authentication) {
        var identity = from(authentication);
        customers.requireByIdentity(identity.issuer(), identity.subject());
        return identity.subject();
    }
    public record FederatedIdentity(String issuer, String subject) {}
}
