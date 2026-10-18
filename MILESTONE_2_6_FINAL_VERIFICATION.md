# NOVARA BANK — MILESTONE 2.6 FINAL VERIFICATION REPORT

Verification date: 18 October 2026 (Asia/Calcutta).

## 1. Executive summary

**Result: PARTIALLY VERIFIED — NOT PRODUCTION READY.** The tested Angular, Spring Security, PostgreSQL, Flyway, CSRF, ownership, step-up rejection, and banking-integrity paths pass. Live Keycloak discovery, client registration, BFF connectivity, PKCE/state/nonce redirect generation, and mandatory TOTP enrollment were exercised. A completed OTP login/callback and cross-browser E2E were not completed, so the definition of done is not met.

## 2. Verified architecture

- PASS — Angular redirects to Spring BFF; no OAuth token storage was found in browser code.
- PASS — Spring uses an ordered stateless bearer chain and CSRF-protected session chain.
- PASS — live redirect carried state, nonce, and S256 PKCE.
- PASS — resource JWT decoder restricts RS256 and validates issuer/audience/standard timestamps.
- PASS — PostgreSQL stores sessions; Flyway V1–V3 migrated cleanly.

## 3. Authentication results

- PASS — Keycloak 26.0.8 started and exposed the expected issuer/discovery/JWKS endpoints.
- PASS — registered client accepted the exact callback URI.
- PASS — temporary user's valid password reached mandatory TOTP enrollment.
- BLOCKED — completed OTP, callback, authenticated API, refresh restoration, and authenticated logout in a real browser.

## 4. MFA and passkeys

- PASS — TOTP is enabled and a default required action; brute-force protection/failure factor 5 verified through the admin API.
- BLOCKED — wrong/expired/repeated OTP, recovery, interruption, and lockout were not executed end-to-end.
- NOT IMPLEMENTED — passkey lifecycle. WebAuthn user verification is configured `required`, but registration/authentication/deletion/recovery were not tested or presented as complete.

## 5. Session security

- PASS — HttpOnly/Lax/path cookie behavior observed; production Secure defaults enforced.
- PASS — 15-minute idle, eight-hour absolute, fixation rotation configuration, three-session limit, local logout, and server-side termination controls.
- PASS — absolute-expiry current-request bypass fixed and unit-tested.
- BLOCKED — multi-instance invalidation and login-time session-ID rotation E2E.

## 6. CSRF

- PASS — missing/invalid POST proof rejected without posting.
- PASS — PUT/PATCH/DELETE without proof rejected.
- PASS — valid authenticated transfer proof succeeded exactly once.
- PASS — malicious origin received no CORS authorization and missing proof was rejected.

## 7. Authorization

- PASS — unknown/restricted identities denied; exact issuer+subject required.
- PASS — cross-customer identifiers do not grant access.
- PASS — operations permission boundary and high-value step-up gate tested.
- BLOCKED — complete workforce-role matrix because corresponding teller/branch/compliance endpoints do not exist.

## 8. Angular results

- PASS — ESLint.
- PASS — 17/17 unit tests.
- PASS — production build, 306.72 kB raw initial bundle (85.52 kB estimated transfer).
- PASS — npm audit, 0 vulnerabilities.

## 9. Spring Boot results

- PASS — 30/30 tests: 26 PostgreSQL integration/security tests and 4 focused session/step-up unit tests.
- PASS — clean PostgreSQL 17.11 Flyway V1–V3 migration.

## 10. Banking regression

PASS — balancing, immutable journals, reversals, holds, ownership, restricted accounts, insufficient funds, rollback, reconciliation, idempotency, and concurrent transfer cases remain green. Authorization/CSRF failures produced no transfer posting.

## 11. Vulnerabilities fixed

| Severity | Root cause | Fix | Verification |
|---|---|---|---|
| High | PKCE not explicitly forced by confidential BFF | Explicit S256 authorization customizer | Live redirect |
| High | Absolute expiry continued current request | Stop chain, clear context, 401 | Unit test |
| High | High-value transfer lacked step-up | Recent provider `auth_time` + MFA `amr` gate | Unit/controller tests |
| Medium | Insecure production secret/cookie fallbacks | Mandatory secret; Secure defaults | Config/live response |
| Medium | CORS omitted mutation methods | Narrow origin with complete required methods | Security tests |

## 12. Remaining vulnerabilities/risks

No unresolved Critical code defect was found in tested scope. High release-evidence gaps remain: completed MFA login/session, browser E2E, global logout decision, key rotation, multi-replica session invalidation, and target-environment gateway/TLS/secret/audit controls.

## 13. Blocked/skipped tests

- BLOCKED — Chromium, Firefox, WebKit E2E: no browser framework/binaries installed.
- BLOCKED — completed MFA/callback and authenticated browser journey.
- BLOCKED — signing-key rotation and multi-instance session tests.
- BLOCKED — Maven/container advisory scanning beyond compilation/tests; npm audit did run.

## 14. Production release blockers

Completed IdP MFA browser flow, authenticated session lifecycle, browser logout/back-tab behavior, global logout policy, key rotation, multi-replica revocation, and deployment controls must be verified before production financial operations.

## 15. Milestone 3 readiness

Development may continue in non-production. Production readiness is **NO** until the blockers above are closed. See the focused OIDC, session, CSRF, authorization, and readiness reports for evidence and limitations.
