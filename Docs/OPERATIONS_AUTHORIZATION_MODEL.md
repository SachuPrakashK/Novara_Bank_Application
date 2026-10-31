# Operations Authorization Model

Proposed permissions: `OPERATIONS_DASHBOARD_READ`, `SERVICE_REQUEST_READ`, `SERVICE_REQUEST_ASSIGN`, `SERVICE_REQUEST_REVIEW`, `SERVICE_REQUEST_RESOLVE`, `CUSTOMER_DIRECTORY_READ`, `ACCOUNT_OVERSIGHT_READ`, `AUDIT_EVENTS_READ`, `OUTBOX_MONITOR_READ`, and `OPERATIONS_CONFIGURATION_READ`.

These are not approved entitlements. Future authorization must combine trusted IdP staff identity, server-side assignment, permission, purpose/scope, recent assurance for sensitive actions, and audited access. Frontend state, query parameters, local storage, customer roles and unverified claims are never authority.

Current real-data status: **DENY ALL**. Synthetic fixtures cannot invoke backend operations APIs.
