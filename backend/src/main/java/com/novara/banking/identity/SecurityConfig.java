package com.novara.banking.identity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean @Order(1)
    SecurityFilterChain bearerApi(HttpSecurity http) throws Exception {
        return http.securityMatcher(request -> {
                String value = request.getHeader("Authorization");
                if (value != null && value.regionMatches(true, 0, "Bearer ", 0, 7)) return true;
                Object context = request.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                return context instanceof SecurityContext securityContext && securityContext.getAuthentication() != null
                    && securityContext.getAuthentication().getPrincipal() instanceof Jwt;
            })
            .csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/**").authenticated().anyRequest().denyAll())
            .oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults())).build();
    }

    @Bean @Order(2)
    SecurityFilterChain browserBff(HttpSecurity http, AbsoluteSessionLifetimeFilter absoluteLifetime,
                                   OidcLoginSuccessHandler successHandler, ClientRegistrationRepository registrations,
                                   @Value("${novara.security.csrf-cookie-secure}") boolean secureCsrfCookie) throws Exception {
        var csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookieName("NOVARA-XSRF-TOKEN");
        csrf.setHeaderName("X-XSRF-TOKEN");
        csrf.setCookiePath("/");
        csrf.setCookieCustomizer(cookie -> cookie.secure(secureCsrfCookie).sameSite("Lax"));
        var authorizationRequests = new DefaultOAuth2AuthorizationRequestResolver(registrations, "/oauth2/authorization");
        authorizationRequests.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return http.cors(Customizer.withDefaults()).csrf(config -> config.csrfTokenRepository(csrf).csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/error", "/oauth2/**", "/login/**", "/api/v1/auth/login", "/api/v1/auth/session").permitAll()
                .requestMatchers("/api/v1/operations/**").hasAnyRole("OPERATIONS", "COMPLIANCE", "ADMINISTRATOR")
                .requestMatchers("/api/v1/auth/**").authenticated()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), request -> request.getRequestURI().startsWith("/api/")))
            .oauth2Login(login -> login.successHandler(successHandler)
                .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(authorizationRequests))
                .userInfoEndpoint(info -> info.oidcUserService(oidcUserService())))
            .addFilterBefore(absoluteLifetime, AuthorizationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.changeSessionId()).maximumSessions(3))
            .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                .deleteCookies("NOVARA_SESSION", "NOVARA-XSRF-TOKEN").invalidateHttpSession(true).clearAuthentication(true)
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; form-action 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'"))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true)))
            .build();
    }

    private OidcUserService oidcUserService() {
        var delegate = new OidcUserService();
        return new OidcUserService() {
            @Override public OidcUser loadUser(OidcUserRequest request) {
                OidcUser user = delegate.loadUser(request);
                Set<GrantedAuthority> authorities = new LinkedHashSet<>(user.getAuthorities());
                Object realmAccess = user.getClaims().get("realm_access");
                if (realmAccess instanceof Map<?, ?> map && map.get("roles") instanceof Collection<?> roles) {
                    roles.stream().map(Object::toString).map(String::toUpperCase)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).forEach(authorities::add);
                }
                return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo(), "sub");
            }
        };
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
                          @Value("${novara.security.audience}") String audience) {
        var decoder = NimbusJwtDecoder.withIssuerLocation(issuer).jwsAlgorithm(SignatureAlgorithm.RS256).build();
        OAuth2TokenValidator<Jwt> audienceValidator = token -> token.getAudience().contains(audience)
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Required audience is missing", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), audienceValidator));
        return decoder;
    }

    @Bean
    CorsConfigurationSource cors(@Value("${novara.cors.allowed-origin}") String origin) {
        var config = new CorsConfiguration(); config.setAllowedOrigins(List.of(origin)); config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Correlation-ID", "X-Request-ID", "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("X-Correlation-ID")); config.setAllowCredentials(true); config.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/api/**", config); return source;
    }
}
