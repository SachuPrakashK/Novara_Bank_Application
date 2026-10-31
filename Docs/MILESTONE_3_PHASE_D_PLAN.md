# Milestone 3 Phase D Plan

## Scope

Deliver an ownership-scoped card-management module on the existing modular monolith. The phase covers card inventory and detail views, processor-acknowledged freeze/unfreeze and control changes, card activity, and issuer-review service requests. It does not issue real cards, dispatch replacements, or integrate a production processor.

## Delivery sequence

1. Verify Phase C lint and backend integration tests.
2. Add Flyway V6 card schema and seeded product definitions.
3. Add the card domain, REST endpoints, processor port, and sandbox adapter.
4. Add lazy Angular card routes, responsive dashboard/detail views, and HTTP/mock adapters.
5. Verify migrations, ownership, state transitions, idempotency, MFA gates, lint, unit tests, and builds.

## Exit criteria

- No Critical or High Phase C blocker.
- Card data is customer-owned and server-derived from authenticated identity.
- Sensitive mutations retain CSRF and recent-MFA enforcement.
- Simulated operations are visibly identified as sandbox behavior.
- Phase D tests pass and known limitations remain documented.
