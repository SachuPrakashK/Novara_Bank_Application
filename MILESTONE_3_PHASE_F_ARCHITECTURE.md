# Phase F Architecture

The Angular Loans route calls an injected `LoansApi`. Production uses HTTP; the local demo environment uses an isolated in-memory adapter. The backend resolves the authenticated customer from issuer plus subject, never from request data.

`LoanService` reads the active product/version snapshot, validates principal and tenure, invokes `LoanEmiCalculator`, and atomically persists the quote and every illustrative installment. Application submission locks the idempotency key, requires an owned, unexpired, unused quote, and records a `SUBMITTED` application plus immutable status event.

The implemented state transition is deliberately narrow:

`product -> quote -> submitted application`

There is no transition to approved, booked, disbursed, active, overdue, repaid, or closed. `loan_accounts` is reserved schema only and receives no rows.

Security controls include authenticated endpoints, issuer/subject ownership checks, CSRF protection, recent identity-provider MFA for submission, request validation, idempotency, audit events, and non-enumerating not-found responses.
