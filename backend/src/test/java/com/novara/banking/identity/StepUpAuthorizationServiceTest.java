package com.novara.banking.identity;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.novara.banking.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StepUpAuthorizationServiceTest {
    private final StepUpAuthorizationService service = new StepUpAuthorizationService(10_000, Duration.ofMinutes(5));

    @Test void acceptsRecentProviderMfaForHighValueTransfer() {
        assertThatCode(() -> service.requireForTransfer(10_000, authentication(Instant.now().minusSeconds(30), List.of("pwd", "otp"))))
            .doesNotThrowAnyException();
    }
    @Test void rejectsStaleOrSingleFactorAuthenticationForHighValueTransfer() {
        assertThatThrownBy(() -> service.requireForTransfer(10_000, authentication(Instant.now().minusSeconds(600), List.of("pwd", "otp")))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> service.requireForTransfer(10_000, authentication(Instant.now(), List.of("pwd")))).isInstanceOf(DomainException.class);
    }
    @Test void doesNotRequireStepUpBelowConfiguredThreshold() {
        assertThatCode(() -> service.requireForTransfer(9_999, authentication(null, List.of("pwd")))).doesNotThrowAnyException();
    }
    private UsernamePasswordAuthenticationToken authentication(Instant authTime, List<String> amr) {
        var builder = Jwt.withTokenValue("test-token").header("alg", "RS256").subject("customer").claim("amr", amr)
            .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300));
        if (authTime != null) builder.claim("auth_time", authTime);
        Jwt jwt = builder.build();
        return UsernamePasswordAuthenticationToken.authenticated(jwt, jwt, List.of());
    }
}
