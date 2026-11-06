# PostgreSQL Recovery Verification

Current execution: 2026-11-06 IST.

The complete Maven test suite connected to Docker Engine 29.8.2, started a fresh PostgreSQL 17 Testcontainers database, and applied all 11 Flyway migrations to an empty schema. Flyway validated 11 migrations and reached version V11.

| Evidence | Result |
|---|---|
| `BankingIntegrationTest` | PASSED 59/59 |
| Complete Maven suite | PASSED 92, SKIPPED 5 provider tests; 97 total, 0 failures/errors |
| PostgreSQL container startup/readiness | PASSED |
| Flyway V1–V11 validation and migration | PASSED |
| Ledger, transfer/idempotency/reversal, payment concurrency, beneficiary, card, deposit, loan, statement, customer-service, authentication/authorization scenarios | PASSED through the 59-test integration suite |
| Workforce authorization POC | PASSED 18/18 |
| Production isolation | PASSED 1/1 |

This is new current evidence. It does not reuse the historical 59/59 result. The persistent local Novara database remained healthy and was not manually repaired or used as the Testcontainers test database.
