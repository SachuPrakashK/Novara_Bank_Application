# Phase I.3.2 Security Matrix

| Scenario | Evidence class | Result |
|---|---|---|
| OIDC discovery/JWKS and service-token cryptography | Prior real Keycloak 26.0.8 | PASS 5/5 |
| Current provider rerun | Environment | NOT RUN — secrets absent; Docker API HTTP 500 |
| Staff mapping, entitlement, scope, revocation, maker-checker | Mock test-only evaluator | PASS 18/18 |
| Production isolation and operations deny-all | Static/test-only isolation | PASS 1/1 |
| Human identities, MFA, browser login, sessions/revocation | Real provider | BLOCKED |
| Backend unit/POC suite excluding PostgreSQL integration | Maven | PASS 33; 5 provider tests SKIPPED |
| PostgreSQL integration/Flyway current rerun | Testcontainers | FAILED ENVIRONMENT — Docker API HTTP 500 before database startup |
| Immutable ledger and customer security | Source boundary review | UNCHANGED |

The previous successful PostgreSQL 59/59 and Flyway V1-V11 evidence remains historical; it is not presented as a current pass.
