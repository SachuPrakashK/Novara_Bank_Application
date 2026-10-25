# Phase F Test Results

Verified on 2026-10-25 with a constrained Java 21 runtime to avoid the prior native-memory failure.

| Check | Result |
|---|---|
| Angular ESLint | Passed, zero warnings |
| Angular unit tests | 20/20 passed across 11 files |
| Angular production build | Passed; lazy `loans-page` bundle emitted |
| Backend tests | 57/57 passed |
| PostgreSQL integration tests | 45/45 passed |
| Flyway validation/migration | V1–V8 passed on PostgreSQL 17 Testcontainers |

Phase F coverage includes zero- and non-zero-rate EMI examples, invalid inputs, deterministic rounding, month-end dates, exact principal reconciliation, persisted schedule equality, product bounds, application idempotency and ownership, CSRF and recent-MFA enforcement, and proof that quotes/applications do not post journals or create loan accounts.

The only build warning is the existing Spring test `@MockBean` deprecation notice; it is unrelated to Phase F correctness.
