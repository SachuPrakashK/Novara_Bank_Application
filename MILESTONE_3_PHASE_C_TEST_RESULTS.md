# Milestone 3 Phase C test results

Verification date: 18 October 2026 (Asia/Calcutta).

| Check | Result |
|---|---|
| Java compile/test compile | PASS |
| PostgreSQL/Testcontainers Spring suite | PASS: 39 tests total (35 integration + 4 focused identity/session) |
| Flyway empty-schema migration | PASS: V1 through V5 on PostgreSQL 17.11 |
| Angular ESLint | PASS: zero warnings/errors |
| Angular unit tests | PASS: 19/19 across 10 files |
| Angular production build | PASS: 314.79 kB raw initial / 87.14 kB estimated transfer; no budget warning |
| Browser E2E (development adapter) | PASS: 2/2 in installed Google Chrome via Playwright |
| Full-stack OIDC E2E | BLOCKED: real Keycloak MFA/session environment remains unavailable |

Integration coverage includes the existing transfer, concurrency, rollback, holds, reversal, reconciliation, CSRF, ownership, and beneficiary suite plus server intent posting/idempotency, beneficiary resolution/cooling, payment limits, and durable schedule cancellation. Playwright verifies development-adapter payment navigation, schedule navigation, and form validation. This is explicitly not full-stack backend or real-IdP evidence. Dedicated multi-worker crash injection and full-stack browser flows remain documented gaps.
