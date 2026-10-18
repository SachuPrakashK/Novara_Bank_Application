# Milestone 3 test results

Verification date: 18 October 2026 (Asia/Calcutta).

Scope: Phase A foundation review and Phase B beneficiary management only.

| Check | Status | Result |
|---|---|---|
| Angular ESLint | PASS | Zero warnings/errors |
| Angular unit tests | PASS | 19/19 across 10 files, including beneficiary HTTP contract tests |
| Angular production build | PASS | 309.44 kB raw initial / 86.02 kB estimated transfer; beneficiary lazy chunk 18.64 kB raw |
| Java compilation | PASS | 43 main sources and 3 test sources compiled on Microsoft OpenJDK 21.0.12.1 |
| Flyway clean migration | PASS | PostgreSQL 17.11 applied V1–V4 from an empty schema |
| Spring unit/integration tests | PASS | 35/35; 31 PostgreSQL integration/security tests plus 4 focused identity/session tests |
| Beneficiary ownership | PASS | Cross-customer lookup returns the same not-found boundary |
| Duplicate/idempotency behavior | PASS | Replay returns original; changed payload and duplicate destination are rejected |
| Verification/cooling lifecycle | PASS | Pending creation, verified activation with future cooling deadline, and restriction on failed verification |
| Deactivation security | PASS | Missing CSRF and missing recent IdP MFA are rejected without state change |
| Existing ledger regression | PASS | The complete pre-existing transfer, journal, holds, reversal, concurrency, authorization, and reconciliation suite remains green |

The suite initially identified explicit PostgreSQL type binding requirements for `timestamptz` and enum parameters in the verification transition. Both were corrected; only the final clean run is acceptance evidence.

Browser E2E was not run because the repository still has no Playwright/Cypress framework or browser binaries. This remains a release-evidence gap, not a passing check.

Phase C superseding verification is recorded in `MILESTONE_3_PHASE_C_TEST_RESULTS.md`: Flyway V1–V5, 39 Spring tests, 19 Angular tests, lint, and the production build pass. Browser E2E remains blocked.

