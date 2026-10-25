# Milestone 3 architecture

## Phase E deposit extension

Phase E adds the `com.novara.banking.deposit` bounded module without introducing another service or ledger. Versioned products feed server-side reviews; confirmed contracts snapshot accepted terms; FD funding and RD installments reuse the existing immutable double-entry journal. The Angular deposit route is standalone and lazy-loaded inside the shared authenticated shell. See `MILESTONE_3_PHASE_E_ARCHITECTURE.md` for boundaries and blocked policy-dependent workflows.

Status: Phases A–C implemented and verified at the automated-test level. Phases D–I have not started. Production release remains blocked by inherited authentication and deployment evidence gaps.

Phase C adds server-authorized payment intents, verified opaque beneficiary resolution, concurrency-safe limits, durable schedules and occurrences, ownership-scoped history and receipts, and a premium Angular payment workspace. See `MILESTONE_3_PHASE_C_ARCHITECTURE.md` for detailed boundaries.

Milestone 3 continues the existing Java 21/Spring Boot modular monolith and Angular standalone application. Beneficiary management is a new bounded module inside the same authenticated API and PostgreSQL transaction boundary; it does not create a parallel identity, persistence, or error-handling architecture.

## Runtime boundaries

- Angular uses a dedicated `BeneficiariesApi` contract selected between an in-memory development adapter and the authenticated HTTP adapter. The lazy-loaded page owns presentation state only.
- Spring Security remains the authority. Browser mutations require the existing session and CSRF proof; bearer requests use the existing stateless resource-server chain.
- `CurrentIdentity` resolves the exact OIDC issuer and immutable subject. Beneficiary reads and writes join that identity to the owning customer in SQL.
- `BeneficiaryService` owns validation beyond request shape, lifecycle transitions, duplicate prevention, idempotency, masking, audit writes, and verification/cooling state.
- Flyway V4 adds beneficiary and verification records without modifying V1–V3.

## Data protection and consistency

Raw destination account identifiers are accepted only for beneficiary creation and are not persisted. PostgreSQL stores a display mask and a keyed HMAC-SHA-256 fingerprint derived from routing code and account identifier. `NOVARA_BENEFICIARY_HASH_KEY` is mandatory and must come from a production secret manager.

Create commands acquire transaction-scoped PostgreSQL advisory locks for both the scoped idempotency key and customer/destination fingerprint. The shared idempotency table binds the caller, operation, key, canonical request fingerprint, and resulting opaque resource identifier. A partial unique index independently prevents concurrent duplicate active destinations.

Beneficiary submission creates `PENDING_VERIFICATION` plus a pending verification attempt. Only the verification service boundary can transition it to `ACTIVE` or `RESTRICTED`; a successful verification sets a server-calculated cooling deadline. Client code does not declare verification success.

## Lifecycle and authorization

Supported states are `PENDING_VERIFICATION`, `ACTIVE`, `RESTRICTED`, and `DEACTIVATED`. Customers may edit display name/nickname and favorite state. Destination identifiers are intentionally immutable through customer profile edits. Deactivation is soft, audited, ownership-scoped, CSRF-protected, and requires recent provider MFA evidence via `amr` plus `auth_time`.

An `ACTIVE` beneficiary with a future `availableFrom` remains in its cooling period and is not payable in the UI. Future payment execution must enforce that same rule again on the backend; Angular is not an authorization boundary.

## Integration limits

The verification completion method is an internal service boundary awaiting an approved verification provider or operations workflow. UPI, IMPS, NEFT, and RTGS are labels/integration boundaries only and are not live rails. No beneficiary operation posts a journal or changes an account balance.

# Phase F status

Phase F adds versioned sandbox loan products, authoritative EMI quotes with durable illustrative schedules, and owned/idempotent application submission. Loan booking and every financial servicing operation remain policy-blocked; see `MILESTONE_3_PHASE_F_ARCHITECTURE.md` and `LOAN_LEDGER_POSTINGS.md`.

# Phase G status

Phase G adds authoritative posted-ledger statements, deterministic balances, server-side transaction filtering/pagination, versioned statement generation metadata, secure PDF/CSV downloads, and posted-journal receipts. Unsupported or unposted financial events never receive receipts. See `MILESTONE_3_PHASE_G_ARCHITECTURE.md` and `STATEMENT_BALANCE_RULES.md`. Phase H has not started.

# Phase G.1 status

Phase G.1 hardens reproducibility with a repeatable-read ledger sequence cutoff, checksum enforcement, PostgreSQL-shared document rate limits, safer CSV/PDF behavior, receipt details, and responsive browser coverage. Production blockers remain documented in `MILESTONE_3_PHASE_G_1_REMAINING_RISKS.md`. Phase H has not started.

# Reversal semantics clarification

Posted journals are immutable. A reversal creates a separate POSTED journal whose `reversal_of_journal_id` references the original journal. The original journal remains POSTED; it is never mutated to REVERSED. Any earlier documentation describing an in-place status change is superseded by this rule.

