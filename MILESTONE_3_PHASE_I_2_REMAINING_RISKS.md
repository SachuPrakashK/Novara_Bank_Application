# Milestone 3 Phase I.2 Remaining Risks

- Authentic Keycloak login, MFA, assurance claims, refresh, suspension, revocation, logout, and session behavior are **BLOCKED**.
- The POC is intentionally in-memory/test-only; it does not prove production database, distributed cache, concurrency, or disaster-recovery behavior.
- The userless realm template requires IAM review before use and is not production configuration.
- No approved staff identity architecture, roles, permissions, scopes, queue ownership, data-access policy, maker-checker classification, audit retention, or operational ownership exists.
- No privileged audit sink or outbox recovery implementation exists.
- A future runtime implementation needs independent threat review, persistence design, key management, provider acceptance, performance testing, and formal approvals.

The current deny-all boundary remains mandatory.
