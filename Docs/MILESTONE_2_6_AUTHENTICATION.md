# Milestone 2.6 — Authentication handoff

## Local setup

1. Copy `.env.example` to `.env` and replace all placeholders. Never commit `.env`.
2. Run `docker compose up -d postgres keycloak`.
3. Start the backend with the same client secret and database settings.
4. Run `npm.cmd run start:oidc`; open `http://localhost:4200`.
5. In Keycloak create a user, assign `customer`, enroll OTP/WebAuthn as required, and capture its immutable `sub`.
6. Create the customer with the exact Keycloak issuer and subject.

Regular `npm.cmd start` keeps the fictional mock. Production and `start:oidc` use:

`Angular → Spring BFF → Keycloak → Spring callback → JDBC session → Angular`

Spring Security validates state and nonce and uses S256 PKCE. OAuth tokens remain server-side. Angular receives only an opaque `HttpOnly` session cookie, CSRF cookie, and minimal session profile.

High-value internal transfers are unavailable unless the trusted authentication context contains recent provider `auth_time` and an `amr` value for OTP, WebAuthn, or MFA. The default threshold is 10,000,000 minor INR units and the maximum authentication age is five minutes; risk owners must approve deployment-specific values.

## Deployment gates

- Register exact HTTPS callback/logout URIs; remove localhost values.
- Enable secure cookies and trust forwarded headers only from known proxies.
- Load client/database secrets from a secret manager.
- Test shared-session revocation across all replicas.
- Configure MFA/recovery, WebAuthn RP origins, SMTP, key rotation, admin separation, and IdP audit export.
- Test fixation, expiry, concurrency, CSRF, redirects, callback errors, disabled users, role removal, and identity mapping.
- Run a real-browser suite against the deployed IdP.

## Honest limitations

This is a foundation, not production-readiness certification. IdP administration is not automated; IdP single-sign-out is not yet propagated; completed MFA callback and cross-browser E2E remain release gates; and passkeys have provider policy but no verified end-to-end lifecycle.
