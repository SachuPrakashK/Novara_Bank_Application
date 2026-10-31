# Milestone 3 — Phase G.1 Test Results

| Verification | Status | Actual result |
|---|---|---|
| Backend compile | PASS | Java 21 compilation succeeded |
| Backend complete suite | PASS | 64/64 tests |
| PostgreSQL integration suite | PASS | 50/50 tests |
| Flyway validation | PASS | V1–V10 applied on PostgreSQL 17 |
| Ledger/payment/card/deposit/loan regressions | PASS | Included in complete suite |
| Historical cutoff/reversal/checksum | PASS | Statement A stable; Statement B reconciled with reversal |
| Idempotency/CSRF/ownership | PASS | Retry, payload conflict, unique record, isolation, CSRF covered |
| Rate limiting | PASS | Third request at test threshold returned 429 and Retry-After |
| PDF structural verification | PASS | Valid PDF, metadata, financial content fixture, 160 rows and multiple pages |
| PDF visual rendering | NOT RUN | No rendered-page visual inspection performed |
| CSV security | PASS | UTF-8, headers/content and formula triggers including whitespace/control prefixes |
| Angular lint | PASS | Zero warnings |
| Angular unit tests | PASS | 20/20 across 11 files |
| Angular production build | PASS | Lazy Statements bundle emitted |
| Statements Playwright | PASS | 2/2 in desktop/mobile Chrome |
| Real Keycloak/MFA acceptance | BLOCKED | Test credentials and automated MFA ceremony unavailable |

The first V10 integration attempt correctly failed due to nanosecond/microsecond checksum serialization. That defect was fixed and both the focused 52-test run and final complete 64-test run passed.
