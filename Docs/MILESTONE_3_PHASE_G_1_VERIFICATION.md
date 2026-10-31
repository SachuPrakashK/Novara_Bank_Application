# Milestone 3 — Phase G.1 Verification

## Baseline

The Phase G baseline was inspected in `StatementService`, `StatementExportService`, `StatementController`, ledger posting/reversal code, V9, identity/security infrastructure, Angular adapters/workspace, tests, and all Phase G documents. Immediately before stabilization, Angular lint, 20 unit tests, production build, 61 backend tests, 48 PostgreSQL integration tests, and Flyway V1–V9 were PASS.

## Financial reconciliation — PASS

A deterministic PostgreSQL scenario posted a 10,000-minor-unit debit from a 100,000 opening balance, issued Statement A, posted the reversing journal, and issued Statement B.

- Statement A: opening 100,000; debits 10,000; credits 0; closing 90,000.
- Statement B: opening 100,000; debits 10,000; credits 10,000; closing 100,000.
- Statement A regenerated after reversal with identical summary and CSV bytes.
- Statement B retained both the original and reversal entries.
- Tampering with the stored checksum caused issuance to fail.

The actual ledger does not mutate the original journal to `REVERSED`; it remains `POSTED` and the separately posted journal carries `reversal_of_journal_id`. Phase G.1 follows that real model.

## Consistency model — PASS

V10 stores the maximum committed journal-line ID visible inside a `REPEATABLE_READ` generation transaction. Rows, opening/closing balances, checksum, PDF, and CSV all use that immutable sequence boundary. Later postings are removed from the current balance projection when reconstructing the cutoff. Wall-clock time remains display metadata, not the authoritative inclusion boundary.

## Security — PASS

Ownership is rechecked for metadata and document bytes. Downloads retain `no-store`, `no-cache`, `nosniff`, fixed server filenames, and attachment disposition. CSRF protects generation. Database-shared customer/endpoint fixed-window limits return HTTP 429 and `Retry-After`.

## Identity-provider acceptance — BLOCKED

Mock-browser and application security tests passed. Real Keycloak login/logout, refresh, expiry, recent MFA, assurance, and post-logout downloads were not executed because no test customer credentials and automated MFA ceremony were supplied. Credentials were not bypassed or embedded.

## PDF visual review — NOT RUN

Programmatic PDF validation passed, including metadata and a 160-row multipage document. This environment did not render and visually inspect the generated pages, so clipping and visual page-break acceptance remain unclaimed.
