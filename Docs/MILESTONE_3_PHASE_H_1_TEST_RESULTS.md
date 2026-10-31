# Milestone 3 Phase H.1 Test Results

Executed 2026-10-26 (Asia/Kolkata).

| Test                                            | Status  | Result                                                                               |
| ----------------------------------------------- | ------- | ------------------------------------------------------------------------------------ |
| Backend compile/test compile                    | PASS    | Java 21 compilation successful                                                       |
| Complete backend suite                          | PASS    | 70/70, zero failures/errors                                                          |
| PostgreSQL integration                          | PASS    | 56/56 on PostgreSQL 17 Testcontainers                                                |
| Flyway                                          | PASS    | V1–V11 validated and applied; no V12 added                                           |
| Ledger/payments/cards/deposits/loans/statements | PASS    | covered by complete suite                                                            |
| Phase H/H.1 integrity                           | PASS    | ownership, CSRF, stale versions, concurrency, idempotency, paging and sensitive text |
| Angular lint                                    | PASS    | zero warnings                                                                        |
| Angular unit tests                              | PASS    | 20/20 across 11 files                                                                |
| Angular production build                        | PASS    | 338.16 kB initial; 92.12 kB estimated transfer                                       |
| Mocked Phase H browser suite                    | PASS    | 3/3 Chromium tests                                                                   |
| Complete Playwright regression                  | PASS    | 18/18 Chromium tests, serial execution                                               |
| Real Keycloak acceptance                        | BLOCKED | controlled identities/MFA automation unavailable                                     |
| Manual screen reader                            | NOT RUN | no manual assistive-technology session performed                                     |
| High-volume performance/load                    | NOT RUN | no production-shaped dataset/environment                                             |

An initial Docker attempt was BLOCKED because Docker Desktop was stopped; it was started and the rerun executed. The first backend implementation run failed three request tests because the advisory-lock `void` result was mapped as a number. The query was corrected to the established `SELECT 1 FROM ...` form and the complete rerun passed. The first mocked browser run was 2/3 because the profile success text lacked a live-region role; after correction, 3/3 passed. A six-worker complete browser run exhausted the constrained dev-server environment after 11 passes; the serial rerun passed 18/18 in 49.7 seconds.
