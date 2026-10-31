# Milestone 3 Phase I.1 Plan

Status: **IMPLEMENTED** for documentation and safe regression controls; privileged operations remain **BLOCKED**.

## Actual baseline

- Spring Security denies every `/api/v1/operations/**` request in both ordered filter chains before controller authorization.
- `CurrentIdentity` maps customer issuer and subject only; it is not a staff directory.
- The only existing operations controller is reconciliation, and the global deny rule makes it unreachable.
- The Angular workspace is development/mock-only, uses an in-memory service without `HttpClient`, and is absent from customer navigation.
- Audit rows are append-only, but no privileged event taxonomy, access explorer, export control, or externally verified tamper-evident store exists.
- The outbox supports pending/published rows only. It has no failure, attempt, quarantine, consumer-health, or replay model.
- Flyway remains V1-V11. No staff entitlement persistence exists.

## Completed sequence

1. Inspect identity, session, audit, outbox, routes, fixtures, and migrations.
2. Strengthen deny-all and synthetic-isolation regression tests.
3. Define proposed staff identity, authorization, segregation-of-duties, data access, queue, workflow, audit, outbox, and API designs.
4. Register unresolved decisions and measurable readiness gates.
5. Run complete backend, frontend, build, and browser regression suites.

No staff user, entitlement, privileged API, financial mutation, approval engine, or V12 migration is introduced.
