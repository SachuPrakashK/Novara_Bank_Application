# Remaining risks

## Production release blockers

- **High — MFA/login E2E evidence:** Keycloak forced TOTP enrollment, but a completed OTP login and Spring callback were not automated. A real authenticated BFF session is therefore unverified.
- **High — browser E2E evidence:** No Playwright/Cypress framework is installed; Chromium, Firefox, and WebKit flows were not run.
- **High — global logout decision:** local BFF logout works, but Keycloak SSO logout is not implemented. Either implement and test RP-initiated logout or formally accept local-only logout with compensating policy.
- **High — deployment validation:** key rotation, multiple BFF replicas, gateway rate limiting, TLS/proxy trust, secret-manager integration, and tamper-evident audit export require target-environment tests.

## Medium/operational risks

- Passkey registration/authentication/deletion/recovery is not implemented end-to-end; only provider WebAuthn policy and enrollment action exist.
- Keycloak user/role lifecycle and immediate application-session invalidation after account suspension are not automated.
- Teller/Branch Manager/Compliance business permissions cannot be fully tested until corresponding endpoints exist.
- Maven advisory/SBOM and container image scanning were not available in this pass; npm audit reported zero vulnerabilities.
- Independent penetration, accessibility, recovery, load, and disaster-recovery testing remain required.
