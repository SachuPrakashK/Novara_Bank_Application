# Milestone 2.5 audit

Audit date: 16 October 2026. Scope: Angular application, Spring Boot API, Flyway schema, PostgreSQL financial behavior, Docker configuration, tests, and documentation.

## Prioritized findings

| Severity | Status | Finding | Root cause and impact | Resolution / verification |
|---|---|---|---|---|
| Critical | Fixed | Concurrent duplicate journal reversal could surface a uniqueness failure after both callers passed the application check. | The existence subquery could use a snapshot established before waiting for a row lock. A second correction could be attempted and fail as an uncontrolled database exception. | Reversal now takes a transaction advisory lock before rechecking and retains the database unique constraint. Sequential and concurrent tests were added. |
| High | Fixed | Available balance equaled ledger balance and ignored reserved funds. | No hold abstraction existed, so an authorized transfer could spend funds reserved for another instruction. | V2 adds active holds, minimum-balance and explicit overdraft fields. Transfers and account APIs calculate available funds under the same account lock. |
| High | Fixed | Idempotency keys were global rather than principal/operation scoped. | A global key caused cross-customer collisions and weakened isolation semantics. | V2 uses `(principal_subject, operation_type, idempotency_key)` uniqueness; lookups and advisory locks use the same scope. Cross-customer and concurrent duplicate tests were added. |
| High | Fixed | Production Angular routes did not match dashboard, transaction, and internal-transfer backend capabilities. | Milestone boundaries left mock-era routes and beneficiary assumptions in the production adapters. | Added authorized dashboard/global transaction APIs, DTO mapping, internal account destinations, and status lookup by idempotency key. |
| High | Release blocker | Browser OIDC login/token acquisition is not configured in this repository. | No identity provider, client ID, redirect URI, or approved BFF was supplied. Inventing a password or token flow would be unsafe. | Backend validation is implemented; deployment must integrate an established OIDC SPA SDK or BFF before production release. |
| Medium | Fixed | Rejected transfer attempts were not retained as security audit events. | Success audit data shared the financial transaction and failures rolled back. | Controller records sanitized denial codes in a `REQUIRES_NEW` audit transaction. |
| Medium | Fixed | Unexpected API exceptions lacked an explicit safe Problem Details fallback. | Framework behavior was safe by configuration but inconsistent. | Added a generic response without exception or SQL details and retained correlation IDs. |
| High | Fixed | Method-level authorization denial was initially converted to HTTP 500 by the generic error fallback. | The broad exception handler intercepted Spring Security's `AccessDeniedException`. | Added a specific safe 403 Problem Details handler and a missing-scope test. |
| Medium | Fixed | Docker ports listened on all host interfaces and included a default database password. | Development convenience could be mistaken for a safe deployment configuration. | Ports now bind to loopback and passwords are mandatory environment values. |
| Medium | Potential / unverified | Cluster-wide rate limiting and tamper-evident audit export are external controls. | Neither can be proven from application source alone. | Documented as deployment requirements and release evidence items. |
| Low | Fixed | An uncertain frontend result had no direct recovery action. | The UI warned users but did not query the idempotency-key endpoint. | Added “Check transfer status” without creating a new payment instruction. |

No large-scale refactor was performed. Existing feature boundaries and visual design were preserved.

