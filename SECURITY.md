# Security model

Angular is not a security boundary. Route guards and idle warnings are UX controls; Spring Security, ownership queries, and database invariants enforce access.

## Browser authentication (Milestone 2.6)

The browser redirects to `GET /api/v1/auth/login`; Spring Security performs OIDC Authorization Code with PKCE and stores the authorized client only in a server-side JDBC session. Passwords, MFA codes, passkeys, access tokens, refresh tokens, and ID tokens are never accepted by or returned to Angular.

- `NOVARA_SESSION` is `HttpOnly`, `SameSite=Lax`, path `/`; deployments must set `NOVARA_SESSION_COOKIE_SECURE=true`.
- Sessions have a 15-minute idle timeout, eight-hour absolute limit, ID rotation on authentication, and a three-session concurrency limit.
- Mutations require `NOVARA-XSRF-TOKEN` in `X-XSRF-TOKEN`.
- Return destinations use a fixed allowlist.
- Customer identity is the exact issuer plus immutable subject, never email or username.

The separate stateless service boundary handles requests carrying `Authorization: Bearer` and validates signature, issuer, expiry, and `novara-api` audience.

The local Keycloak realm disables self-registration/password grants, enables brute-force defenses, defines customer/workforce roles, requires S256 PKCE, and exposes TOTP/WebAuthn actions. It has no users or credentials.

Step-up for high-risk transactions is an extension point, not claimed complete. Production policy must initiate fresh OIDC authorization with `acr`/`max_age`, verify `acr`, `amr`, and `auth_time` server-side, bind assurance to the transaction, and expire it quickly.

Never log credentials, tokens, session IDs, CSRF values, full account numbers, or financial request bodies. Before deployment, use TLS and a secret manager, enable secure cookies, restrict proxy headers, rate-limit at the edge, separate database roles, export audits to tamper-evident storage, and run real-browser IdP tests.

Beneficiary destinations are persisted only as a display mask plus a keyed HMAC fingerprint. Production must supply `NOVARA_BENEFICIARY_HASH_KEY` from a secret manager. Beneficiary creation is idempotent and pending verification; sensitive deactivation requires recent IdP MFA evidence. Destination changes require a new verified beneficiary rather than an in-place edit.
## Phase C payment controls

Payment confirmation ignores client financial details and uses the locked server intent. Beneficiary ownership, verified resolution, cooling period, account eligibility, limits, CSRF, and provider-backed step-up policy are enforced server-side. Receipt, history, and schedule access is issuer-and-subject ownership scoped.
