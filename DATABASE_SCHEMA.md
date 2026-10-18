# Database schema

Flyway owns the schema in `backend/src/main/resources/db/migration`.

- `customers`, `account_owners`, `bank_accounts`: identity mapping and account ownership.
- `ledger_accounts`, `ledger_balances`: chart entries and atomic projections.
- `posting_batches`, `journals`, `journal_lines`: immutable double-entry authority.
- `transfers`, `idempotency_records`: workflow result and replay protection.
- `account_holds`: active/released/captured/expired reservations used only in the available-balance projection.
- `audit_events`, `outbox_events`: append-only audit and post-commit integration intent.
- `reconciliation_runs`, `reconciliation_exceptions`: operational findings.
- `beneficiaries`, `beneficiary_verifications`: ownership-scoped beneficiary lifecycle, masked destinations, keyed duplicate fingerprints, cooling deadlines, and append-only verification attempts.

Sequential keys are private database joins. Public APIs use unique UUIDs. Full account numbers are represented by ciphertext plus a deterministic lookup hash and a separately stored mask; key management and encryption implementation must be integrated with the deployment secret-management platform before account origination is enabled.

V2 scopes idempotency uniqueness by principal, operation, and key; adds zero-default minimum-balance and overdraft fields; and makes audit rows update/delete immutable. Available balance subtracts active, unexpired holds while ledger balance remains journal-derived.

Production should use separate roles: a migration role that owns schema objects, an application role limited to required DML/function execution, and read-only operational roles. Backups require encrypted storage, restore drills, point-in-time recovery, and reconciliations after restore.
## Phase C payments

Flyway V5 adds payment intents/status events, internal destination resolution, payment limit policies, durable schedules, and stable occurrences. Completed payment state cannot be changed backward. Customer/date and due-schedule indexes support history and worker claiming.
