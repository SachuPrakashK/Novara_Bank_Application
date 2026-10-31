# Milestone 3 Phase C architecture

Phase C extends the existing modular monolith. `PaymentService` owns immutable server-side review intents, eligibility and limit orchestration; `InternalTransferService` and `LedgerPostingService` remain the sole posting path. A confirmed intent links to one transfer and one balanced journal.

Beneficiary payment resolution is explicit: V5 adds a nullable foreign key to an eligible internal account. Only the trusted verification boundary can supply that opaque reference. Fingerprints remain non-reversible, unresolved beneficiaries remain ineligible, and raw account numbers are not added.

Limits are checked after a customer-scoped PostgreSQL advisory lock inside the confirmation transaction. Completed principal plus fee counts toward the Asia/Kolkata banking day. Failed, cancelled, pending, and reversed instructions do not count. The supported internal rail currently has zero fees.

Schedules are durable PostgreSQL records. Workers claim due rows with `FOR UPDATE SKIP LOCKED` and short leases. Each occurrence has a stable unique number and idempotency key. Execution recreates current validation; instructions requiring fresh MFA become `REQUIRES_REVIEW` instead of bypassing assurance.

Angular uses the payment API contract for server review, confirmation, history, limits, schedules, detail, and receipts. The payment overview and supporting pages use the shared Novara design language documented in `NOVARA_DESIGN_SYSTEM.md`.
