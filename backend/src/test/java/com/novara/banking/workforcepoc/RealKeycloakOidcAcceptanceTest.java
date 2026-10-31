package com.novara.banking.workforcepoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@EnabledIfEnvironmentVariable(named = "NOVARA_WORKFORCE_REAL_PROVIDER_TESTS", matches = "true")
class RealKeycloakOidcAcceptanceTest {
    private static final String ISSUER = "http://127.0.0.1:8082/realms/novara-workforce-test";
    private static final String AUDIENCE = "novara-workforce-poc-api";
    private static final String CLIENT = "novara-workforce-verifier";
    private final ObjectMapper json = new ObjectMapper();

    @Test void exposesExpectedDiscoveryAndPublishedJwks() throws Exception {
        Map<String,Object> discovery = getJson(ISSUER + "/.well-known/openid-configuration");
        assertThat(discovery.get("issuer")).isEqualTo(ISSUER);
        assertThat(discovery.get("jwks_uri")).isEqualTo(ISSUER + "/protocol/openid-connect/certs");
        Map<String,Object> jwks = getJson(discovery.get("jwks_uri").toString());
        assertThat((List<?>) jwks.get("keys")).isNotEmpty();
    }

    @Test void validatesActualKeycloakSignatureIssuerAudienceClientAndExpiry() throws Exception {
        String token = serviceToken();
        Jwt jwt = decoder(AUDIENCE).decode(token);
        assertThat(jwt.getIssuer().toString()).isEqualTo(ISSUER);
        assertThat(jwt.getAudience()).contains(AUDIENCE);
        assertThat(jwt.getClaimAsString("azp")).isEqualTo(CLIENT);
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
        assertThat(jwt.getSubject()).isNotBlank();
    }

    @Test void rejectsTamperedTokenAndWrongAudienceUsingActualProviderKey() throws Exception {
        String token = serviceToken();
        char replacement = token.endsWith("A") ? 'B' : 'A';
        String tampered = token.substring(0, token.length() - 1) + replacement;
        assertThatThrownBy(() -> decoder(AUDIENCE).decode(tampered)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> decoder("wrong-audience").decode(token)).isInstanceOf(JwtException.class);
    }

    @Test void rejectsActualProviderTokenWhenExpectedIssuerIsWrong() throws Exception {
        String token = serviceToken();
        var decoder = NimbusJwtDecoder.withJwkSetUri(ISSUER + "/protocol/openid-connect/certs").build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER + "-wrong"));
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test void rejectsActualProviderTokenAfterItsShortTestLifetime() throws Exception {
        String token = serviceToken();
        Jwt jwt = decoder(AUDIENCE).decode(token);
        long waitMillis = Math.max(0, Duration.between(Instant.now(), jwt.getExpiresAt().plusSeconds(1)).toMillis());
        assertThat(waitMillis).as("disposable realm token lifetime must remain bounded").isLessThanOrEqualTo(25_000);
        Thread.sleep(waitMillis);
        assertThat(Instant.now()).isAfter(jwt.getExpiresAt());
        assertThatThrownBy(() -> strictTimestampDecoder().decode(token)).isInstanceOf(JwtException.class);
    }

    private NimbusJwtDecoder decoder(String expectedAudience) {
        var decoder = NimbusJwtDecoder.withIssuerLocation(ISSUER).build();
        var audience = (org.springframework.security.oauth2.core.OAuth2TokenValidator<Jwt>) jwt ->
            jwt.getAudience().contains(expectedAudience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Required audience is missing", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(ISSUER), audience));
        return decoder;
    }

    private NimbusJwtDecoder strictTimestampDecoder() {
        var decoder = NimbusJwtDecoder.withJwkSetUri(ISSUER + "/protocol/openid-connect/certs").build();
        var timestamp = new JwtTimestampValidator(Duration.ZERO);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamp, new JwtIssuerValidator(ISSUER)));
        return decoder;
    }

    private String serviceToken() throws Exception {
        String secret = System.getenv("NOVARA_WORKFORCE_VERIFIER_SECRET");
        assertThat(secret).as("verifier secret must be injected").isNotBlank();
        String form = "grant_type=client_credentials&client_id=" + CLIENT + "&client_secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8);
        var request = HttpRequest.newBuilder(URI.create(ISSUER + "/protocol/openid-connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String,Object> body = json.readValue(response.body(), new TypeReference<>() {});
        return body.get("access_token").toString();
    }

    private Map<String,Object> getJson(String uri) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(uri)).GET().build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readValue(response.body(), new TypeReference<>() {});
    }
}
