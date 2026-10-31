# Phase I.3.1 Security Test Matrix

| Scenario | Classification | Current result |
|---|---|---|
| Customer escalation; wrong issuer/audience/client | VERIFIED WITH MOCKS | DENIED |
| Forged role/permission | VERIFIED WITH MOCKS / BY CONSTRUCTION | NON-AUTHORITATIVE |
| Invalid signature evidence; expired token | VERIFIED WITH MOCKS | DENIED |
| Missing/stale MFA | VERIFIED WITH MOCKS | DENIED |
| Revoked/expired entitlement; suspended/unmapped staff | VERIFIED WITH MOCKS | DENIED |
| Cross-org/queue/resource and synthetic IDOR | VERIFIED WITH MOCKS | DENIED |
| Self/unauthorized/duplicate/stale approval | VERIFIED WITH MOCKS | DENIED |
| Production POC class/resource absence | AUTOMATED STATIC TEST | PASSED 1/1 |
| Real Keycloak signature/issuer/audience/client | REAL KEYCLOAK | BLOCKED; 2 tests skipped |
| Real tamper/wrong audience | REAL KEYCLOAK | BLOCKED |
| Customer/workforce cross-provider rejection | REAL PROVIDERS | BLOCKED |
| Human MFA/login/session/logout/revocation | REAL KEYCLOAK/BROWSER | BLOCKED |
| Operations deny-all | FRONTEND + EXISTING CONFIG | PASSED regression; Spring rules statically unchanged |
| PostgreSQL operations deny-all integration | TESTCONTAINERS | VERIFIED in 59/59 integration suite |

Categories are intentionally not combined into a single security pass count.
