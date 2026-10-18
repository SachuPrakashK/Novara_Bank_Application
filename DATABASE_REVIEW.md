# Database review

V1 remains unchanged. Forward-only migration `V2__security_and_ledger_hardening.sql` adds account reservations, explicit minimum/overdraft fields, principal/operation-scoped idempotency, supporting indexes, and audit deletion/update protection.

Foreign keys cover ownership, accounts, ledger lines, journals, transfers, holds, reconciliation exceptions, and balance projections. Unique constraints protect public identifiers, account-number hashes, ledger mappings, posting references, reversal targets, hold references, and scoped idempotency keys. Check constraints enforce positive money and supported currencies.

Flyway V1→V2 execution is verified through PostgreSQL Testcontainers. Production must use separate migration/runtime roles; the runtime role should have no DDL, journal mutation, audit mutation, or broad schema privileges. Backup restore, point-in-time recovery, encryption, and replica/failover evidence remain operational responsibilities.

