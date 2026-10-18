# Test results

Verification date: 18 October 2026 (Asia/Calcutta). These results were executed during the final-verification pass.

| Check | Status | Result |
|---|---|---|
| Angular ESLint | PASS | Zero warnings/errors |
| Angular unit tests | PASS | 17/17 |
| Angular production build | PASS | Initial bundle 306.72 kB raw / 85.52 kB estimated transfer |
| npm audit | PASS | 0 vulnerabilities |
| Keycloak discovery/client | PASS | Realm issuer/discovery and registered client redirect exercised live |
| Live BFF startup | PASS | Connected to Keycloak and isolated PostgreSQL; health UP; Flyway V1–V3 applied |
| Live authorization request | PASS | State, nonce, and S256 PKCE observed |
| MFA policy/enrollment | PASS | Mandatory TOTP enrollment reached after valid password |
| Completed MFA login/callback | BLOCKED | OTP enrollment/challenge not completed in browser automation |
| Cross-browser E2E | BLOCKED | No E2E framework/browsers installed |
| Backend unit/integration suite | PASS | 30/30; PostgreSQL 17.11 Testcontainers; Flyway V1–V3 |

The initial backend baseline was temporarily BLOCKED while Docker Desktop was stopped. After Docker started, the PostgreSQL-backed suite was run repeatedly during fixes. Intermediate failures exposed and corrected test/configuration defects; only the final clean run is used for acceptance.
