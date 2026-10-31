# Novara backend architecture

The Milestone 2 backend is a Java 21 / Spring Boot 3.5 modular monolith in `backend/`. Account, ledger, and transfer code intentionally share one PostgreSQL database and one transaction manager. Splitting these responsibilities into independently committed services would create avoidable financial consistency failure modes.

## Modules

- `identity`: OIDC resource-server validation, deny-by-default HTTP policy, customer and operations authorities.
- `customer`: identity-subject mapping and the minimal customer profile.
- `account`: ownership-filtered account, balance, and journal-derived transaction queries.
- `ledger`: balanced immutable journal posting and controlled reversal entries.
- `transfer`: authorization, deterministic locking, idempotency, posting, audit, and outbox orchestration.
- `audit`: append-only security and business audit records.
- `reconciliation`: detection-only consistency checks.
- `beneficiary`: ownership-scoped beneficiary lifecycle, idempotent creation, duplicate prevention, verification/cooling states, and MFA-gated deactivation.
- `shared`: correlation IDs and RFC 9457-style Problem Details.

## Consistency boundary

An internal transfer acquires a transaction-scoped advisory lock for the idempotency key, then locks both account and balance rows in ascending opaque public-ID order. It verifies ownership and restrictions after locking, posts debit and credit lines, updates both projections, records the transfer, audit record, and outbox event, and only then commits. The response is returned after Spring's transactional interceptor completes the durable commit.

The advisory idempotency lock and uniqueness key are scoped to authenticated principal, operation type, and caller key. Account holds use the same account row lock as transfers; available funds are recomputed under that lock before debit posting.

This order prevents double spending because concurrent debits from the same source serialize on the same account/balance row and re-read the committed projection before posting. Sorting both account UUIDs prevents opposing transfers from acquiring locks in opposite order.

Spring Boot 3.5.16 was selected because it is a currently supported stable line with mature Spring Security and testing ecosystem support for Java 21.
## Advanced payments

The payment module orchestrates server-owned review intents and the existing internal-transfer/ledger services. It adds no parallel balance mutation path. Limit enforcement and posting share one PostgreSQL transaction; schedule workers use leased `SKIP LOCKED` claiming and stable occurrence idempotency.
