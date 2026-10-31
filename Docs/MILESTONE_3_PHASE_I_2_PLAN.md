# Milestone 3 Phase I.2 Plan

Status: **IMPLEMENTED** for isolated synthetic proof; production staff operations remain **BLOCKED**.

## Inspected baseline

- Customer Keycloak realm/client exists locally, but no separate workforce realm, disposable staff identities, vault-backed POC secret, or repeatable authentic MFA automation existed.
- Browser and bearer Spring chains deny `/api/v1/operations/**`; customer `CurrentIdentity` must remain unchanged.
- Session fixation, CSRF, idle/absolute expiry, logout, issuer/audience validation, and customer recent-MFA checks already have mock/integration coverage.
- Phase I demo is development/mock-only and has no HTTP dependency.
- Flyway V1-V11 has no workforce tables; real operations and financial mutations are unavailable.

## Safe implementation

1. Add a test-source-only workforce resolver and authorization evaluator.
2. Add synthetic in-memory entitlement lifecycle and segregation-of-duties models.
3. Add a separate workforce Keycloak import template containing no users or credentials.
4. Keep the template outside ordinary Docker mounts and every production profile.
5. Test identity separation, token evidence, MFA evidence consumption, scope, validity, revocation, concurrency, and approval rules with mocks.
6. Mark real-provider authentication/MFA/lifecycle tests blocked until controlled infrastructure exists.

No runtime endpoint, Angular staff page, database migration, staff seed, or financial code change is justified.
