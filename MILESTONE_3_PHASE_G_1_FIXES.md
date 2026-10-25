# Milestone 3 — Phase G.1 Fixes

## Confirmed defects and corrections

1. **Timestamp-only cutoff race.** V9 could not prove a stable committed snapshot under concurrent posting. V10 adds `accounting_cutoff_line_id`; statement reads use `REPEATABLE_READ` and the immutable ledger sequence boundary.
2. **Unchecked stored checksum.** Regenerated downloads now recompute and compare financial content. Legacy rows without a sequence cutoff are blocked instead of presented as reproducible.
3. **Timestamp serialization mismatch.** Java nanoseconds and PostgreSQL microseconds produced false checksum failures. Hash input now canonicalizes database timestamp formatting while retaining financial rows, identities, sequence/order, dates, and balances.
4. **No distributed download limiter.** A PostgreSQL atomic fixed-window limiter now scopes usage by customer and endpoint, with configurable sandbox limits, HTTP 429, and `Retry-After`.
5. **CSV whitespace bypass.** Formula neutralization now detects trigger characters after leading whitespace and covers tab/carriage-return cases.
6. **PDF readiness gaps.** Documents now contain title/author metadata and page numbers. Unsupported Windows-1252 content is rejected explicitly rather than rendered as missing glyphs.
7. **Statements UI gaps.** Added date presets, explicit sorting, account-change refresh, retry feedback, receipt detail modal, receipt PDF action, and focused mobile overflow coverage.

## Principal files changed

- `V10__statement_cutoff_and_rate_limits.sql`
- `StatementService`, `StatementExportService`, `StatementController`
- `DocumentRateLimitService`, `DocumentRateLimitException`, `ApiExceptionHandler`
- Statement Angular models/adapters/page/template/styles
- `BankingIntegrationTest`, `StatementExportServiceTest`
- `statements-receipts.spec.ts`
