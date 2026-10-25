# Milestone 3 Phase E.1 Fixes

## Backend and database

- Qualified the RD occurrence schedule projection to remove PostgreSQL's ambiguous `status` failure.
- Added a configurable initial delay to the RD worker and isolated scheduled execution during integration tests.
- Added integration coverage for FD atomic funding, rollback, ownership, replay and concurrent confirmation.
- Added integration coverage for RD occurrence sequencing, stable idempotency keys, concurrent worker claims, duplicate prevention, and insufficient funds.

## Angular and UI

- Completed card transaction filters and server-side page navigation on the card detail page.
- Added a typed card activity query/page contract and HTTP parameter regression test.
- Added an RD installment summary and explicit unavailable messaging for policy-blocked deposit operations.
- Added a Playwright card-filter workflow test.

## Files principally changed

- `backend/src/main/java/com/novara/banking/deposit/DepositService.java`
- `backend/src/main/java/com/novara/banking/deposit/DepositInstallmentService.java`
- `backend/src/test/java/com/novara/banking/BankingIntegrationTest.java`
- `src/app/core/cards/*`
- `src/app/features/cards/card-details.page.*`
- `src/app/features/deposits/deposits.page.*`
- `e2e/card-activity-filters.spec.ts`

No architecture replacement, new ledger, custom authentication flow, or Phase F work was introduced.
