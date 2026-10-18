package com.novara.banking.identity;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import com.novara.banking.shared.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class StepUpAuthorizationService {
    private final long transferThresholdMinor;
    private final Duration maximumAge;
    public StepUpAuthorizationService(@Value("${novara.security.step-up-transfer-threshold-minor}") long transferThresholdMinor,
                                      @Value("${novara.security.step-up-max-age}") Duration maximumAge) {
        this.transferThresholdMinor = transferThresholdMinor; this.maximumAge = maximumAge;
    }

    public void requireForTransfer(long amountMinor, Authentication authentication) {
        if (amountMinor < transferThresholdMinor) return;
        requireForSensitiveAction(authentication, "Recent identity-provider MFA is required for this transfer.");
    }

    public void requireForSensitiveAction(Authentication authentication, String message) {
        Instant authenticatedAt = claimAsInstant(authentication.getPrincipal(), "auth_time");
        Object amr = claim(authentication.getPrincipal(), "amr");
        boolean strongMethod = amr instanceof Collection<?> methods && methods.stream()
            .map(Object::toString).anyMatch(method -> method.equals("otp") || method.equals("webauthn") || method.equals("mfa"));
        boolean recent = authenticatedAt != null && authenticatedAt.plus(maximumAge).isAfter(Instant.now());
        if (!strongMethod || !recent) {
            throw new DomainException(HttpStatus.FORBIDDEN, "STEP_UP_REQUIRED", message);
        }
    }

    private Object claim(Object principal, String name) {
        if (principal instanceof Jwt jwt) return jwt.getClaims().get(name);
        if (principal instanceof OidcUser oidc) return oidc.getClaims().get(name);
        return null;
    }
    private Instant claimAsInstant(Object principal, String name) {
        Object value = claim(principal, name);
        if (value instanceof Instant instant) return instant;
        if (value instanceof Number epoch) return Instant.ofEpochSecond(epoch.longValue());
        return null;
    }
}
