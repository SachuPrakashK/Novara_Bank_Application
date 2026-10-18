# Authentication architecture

Novara uses `Angular → Spring Boot BFF → OIDC provider`. Angular follows redirects and sends an opaque session cookie; it never receives OAuth tokens. Spring Security performs Authorization Code flow with explicitly forced S256 PKCE, state and OIDC nonce handling, ID-token validation, and server-side authorized-client storage in PostgreSQL-backed Spring Session.

Browser requests use the session security chain and CSRF. Requests presenting a bearer credential use the ordered stateless resource-server chain and validate RS256, issuer, expiry, and `novara-api` audience. A browser cannot select that chain without possessing a valid bearer token; tokens are never exposed to the Novara browser application.

Customer authorization uses exact issuer plus subject and requires an ACTIVE customer. Workforce operations require explicit roles/scopes. High-value transfers require recent provider-issued `auth_time` plus `amr` containing OTP, WebAuthn, or MFA; otherwise the operation is unavailable with `STEP_UP_REQUIRED`.
