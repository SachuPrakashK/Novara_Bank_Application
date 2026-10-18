# Milestone 3 Phase C implementation plan

Status: implementation in progress. Phase D is out of scope.

## Inspection decisions

- Keep `InternalTransferService` and `LedgerPostingService` as the only internal-money posting path. Payment confirmation orchestrates them; it does not update balances.
- Replace the browser-generated quote with a short-lived, immutable server-side payment intent. Confirmation accepts only the intent identifier and an idempotency key.
- Resolve a beneficiary only through a verified opaque `resolved_account_id`. The HMAC fingerprint remains non-reversible and is never treated as an account identifier. Existing unresolved beneficiaries remain unpayable.
- Serialize limit decisions per customer inside the posting transaction. Completed payments count toward the Asia/Kolkata banking day; fees are zero for the currently supported internal rail. Failed, cancelled, and reversed instructions do not count.
- Persist schedules and recurring occurrences in PostgreSQL. Workers claim due rows with `FOR UPDATE SKIP LOCKED`; stable occurrence keys make ledger posting idempotent across retries.
- Keep UPI, IMPS, NEFT, and RTGS unavailable. Phase C supports only Novara internal transfers.

## Sequence

1. Add forward-only V5 payment intent, lifecycle, limit-policy, schedule, occurrence, receipt metadata, indexes, and beneficiary resolution schema.
2. Add the payment domain around the existing transfer engine: intent review, eligibility, limit enforcement, confirmation, ownership-scoped history/detail, schedules, worker recovery, and receipts.
3. Add PostgreSQL integration tests for intent immutability, beneficiary rules, ownership, idempotency, limits, schedules, and ledger invariants.
4. Replace the Angular payment adapter and flow with real APIs, then add overview, history, schedules, limits, detail, and receipt experiences.
5. Establish reusable premium UI primitives and a written design-language standard for Phase C and all future Cards, Deposits, Loans, Statements, and Administration modules.
6. Run clean Flyway, Spring/PostgreSQL tests, Angular lint/tests/build, and available browser E2E. Record blocked evidence accurately.

## Security boundaries

- Issuer and immutable subject resolve the customer; clients cannot provide a customer identity.
- Browser mutations retain the existing CSRF filter chain. Opaque UUIDs and ownership joins prevent enumeration.
- High-value confirmation requires recent provider-backed MFA. Scheduled high-risk occurrences enter `REQUIRES_REVIEW`; workers never synthesize MFA evidence.
- Request amounts, destinations, fees, and currency are read from the locked intent during confirmation.
- External rails never return fictional success.

## Verification gate

Critical financial, ownership, concurrency, or migration failures block completion. Existing Milestone 2.6 release blockers remain release blockers even if Phase C automated checks pass.
