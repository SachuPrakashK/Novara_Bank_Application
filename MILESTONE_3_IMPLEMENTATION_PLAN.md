# Novara Bank Milestone 3 implementation plan

Status: in progress. Milestone 3 development is restricted to fictional, non-production data until the open Milestone 2.6 authentication and deployment controls are independently verified.

## Phase A — foundation review

1. Preserve the Java 21/Spring Boot modular-monolith consistency boundary and the Angular standalone/lazy-loaded feature architecture.
2. Establish clean frontend lint, unit-test, and production-build baselines plus the PostgreSQL-backed Spring test baseline.
3. Extend Flyway only with forward migrations; do not alter V1–V3.
4. Reuse issuer-and-subject identity mapping, ownership-scoped SQL, RFC 9457 errors, CSRF/session controls, audit records, and the established idempotency patterns.
5. Keep the existing Milestone 2.6 blockers visible. Beneficiary functionality does not constitute production authentication verification.

## Phase B — beneficiary management

### Backend

- Add beneficiary and verification lifecycle tables with opaque public identifiers, customer ownership, encrypted-or-tokenized account-reference boundaries, deterministic duplicate lookup, masked display values, status constraints, cooling-period timestamps, optimistic versioning, and supporting indexes.
- Implement a dedicated beneficiary module with list/detail/create/update/favorite/deactivate commands. Every query and mutation is scoped to the authenticated issuer and subject.
- Require caller-supplied idempotency keys for creation and reject key reuse with a different canonical request.
- Create beneficiaries as `PENDING_VERIFICATION`; never infer verification from form submission. Keep verification activation behind a controlled service boundary and cooling-period policy.
- Require verified identity-provider-backed step-up evidence for high-risk identifier changes and deactivation. If assurance is absent, reject the operation.
- Append audit events for accepted lifecycle changes without recording raw account identifiers.
- Add validation, stable safe errors, duplicate handling, and integration tests covering ownership, idempotency, duplicates, lifecycle states, and step-up rejection.

### Frontend

- Add a beneficiary-specific API contract, HTTP adapter, DTO mapping, and state suitable for loading/error/empty/filter states.
- Replace the existing single embedded form/list with responsive list, detail, add, permitted edit, favorite, and deactivate interactions.
- Mask identifiers, expose verification and cooling states accurately, prevent repeated submissions, and make unavailable high-risk actions explicit.
- Retain existing Novara design tokens, dark-mode compatibility, keyboard/focus semantics, and responsive layouts.
- Add focused unit tests for adapter and state behavior.

### Verification gate

- Run frontend lint, unit tests, and production build.
- Run Spring unit/integration tests against PostgreSQL/Testcontainers and verify Flyway V1–V4 from a clean database.
- Document actual architecture, design decisions, test evidence, and unresolved risks before considering Phase C.

## Later phases

Phase C advanced internal payments, limits, schedules, history, receipts, and premium UI is implemented and verified as recorded in the Phase C documents. Phases D–I remain gated in order: card management; fixed/recurring deposits; loan servicing; statements; dashboard and notifications; then full regression and security verification. No later phase is considered started or complete.
