# Milestone 3 Phase I.3.2 Test Results

The isolated provider ran and real non-human OIDC acceptance passed 5/5. Backend, PostgreSQL, Angular, build, focused browser, and complete browser regressions passed; cleanup succeeded.

- **REAL KEYCLOAK:** runtime/import, discovery/JWKS, service-token signature/claims, tamper, wrong audience/issuer, expiry.
- **MOCKS:** mapping, entitlement, scopes, revocation, segregation of duties, negative authorization.
- **STATIC/INTEGRATION:** production isolation and operations deny-all.
- **BLOCKED:** human identities, MFA, browser code flow, human sessions/revocation, future `nbf`, formal approvals.

Local technical success is not organizational or production acceptance.
