package com.novara.banking.identity;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

import com.novara.banking.customer.CustomerProfile;
import com.novara.banking.customer.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    static final String RETURN_TO = "NOVARA_RETURN_TO";
    private static final Set<String> ALLOWED_DESTINATIONS = Set.of("/dashboard", "/accounts", "/payments", "/security", "/profile");
    private final CustomerRepository customers;
    private final CurrentIdentity identities;

    public AuthenticationController(CustomerRepository customers, CurrentIdentity identities) {
        this.customers = customers; this.identities = identities;
    }

    @GetMapping("/login")
    void login(@RequestParam(defaultValue = "/dashboard") String returnTo, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession(true).setAttribute(RETURN_TO, ALLOWED_DESTINATIONS.contains(returnTo) ? returnTo : "/dashboard");
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/novara");
    }

    @GetMapping("/session")
    SessionView session(Authentication authentication, HttpServletRequest request, CsrfToken csrfToken) {
        csrfToken.getToken();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) return SessionView.anonymous();
        return authenticatedSession(authentication, request.getSession(false));
    }

    @PostMapping("/session/continue")
    SessionView continueSession(Authentication authentication, HttpServletRequest request) {
        return authenticatedSession(authentication, request.getSession(false));
    }

    private SessionView authenticatedSession(Authentication authentication, HttpSession session) {
        var identity = identities.from(authentication);
        CustomerProfile customer = customers.requireByIdentity(identity.issuer(), identity.subject());
        String name = customer.displayName().trim();
        String firstName = name.isEmpty() ? "Customer" : name.split("\\s+")[0];
        String initials = name.isEmpty() ? "NB" : name.codePoints().filter(Character::isLetter).limit(2)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString().toUpperCase();
        Instant expiresAt = Instant.ofEpochMilli(session.getLastAccessedTime()).plusSeconds(session.getMaxInactiveInterval());
        return new SessionView(true, new CustomerView(customer.id().toString(), firstName, initials, "Customer"), expiresAt.toString());
    }

    record SessionView(boolean authenticated, CustomerView customer, String expiresAt) {
        static SessionView anonymous() { return new SessionView(false, null, null); }
    }
    record CustomerView(String id, String firstName, String initials, String tier) {}
}
