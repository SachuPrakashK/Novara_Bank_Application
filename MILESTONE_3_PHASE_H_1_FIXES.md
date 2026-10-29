# Milestone 3 Phase H.1 Fixes

- Serialized service-request idempotency keys with a PostgreSQL transaction advisory lock, preventing concurrent duplicate retries from surfacing uniqueness failures.
- Added bounded service-request pagination, deterministic sorting and customer-scoped description/category search.
- Rejected descriptions containing password, passcode, OTP, PIN or recovery-code terms before persistence.
- Made status-history ordering deterministic by timestamp and event ID.
- Added page metadata and deterministic ordering to notification history.
- Added functional mock self-service adapters so browser tests do not call nonexistent APIs in demo mode; all identity assurance is labeled sandbox-only.
- Added loading, empty, error, retry, duplicate-submit prevention, search, sort and pagination states to Phase H pages.
- Added accessible status announcements and mobile overflow verification.
- Corrected the Milestone 3 implementation-status narrative while preserving reversal semantics.

No V12 was justified: existing V11 indexes and uniqueness constraints support these changes. V1–V11 remain unchanged.
