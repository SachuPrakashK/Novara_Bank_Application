# Milestone 3 Phase I.3 Plan

Status: **PARTIALLY IMPLEMENTED; REAL PROVIDER EXECUTION BLOCKED**

## Actual baseline

- Customer BFF uses the `novara` realm/client, validates RS256 issuer and `novara-api` audience, uses PKCE, session fixation protection, secure/SameSite cookies, CSRF, idle/absolute expiry, and deny-all operations rules.
- Phase I.2 workforce policy code exists only in test sources and has 18 tests. The earlier 17 count preceded the explicit suspended-staff test; 18 is the current verified count.
- The userless workforce realm template was not mounted by normal Compose and contained no identities.
- No approved disposable staff identities, MFA devices, or vault-backed secrets were available.

## Implemented sequence

1. Add a pinned, localhost-only, disposable workforce Compose environment with tmpfs storage.
2. Add process-secret-driven start/stop and read-only preflight scripts.
3. Add a separate verifier service client for real signed-token/JWKS acceptance without staff privileges.
4. Add conditional real-Keycloak cryptographic tests and unconditional production-isolation tests.
5. Preserve the customer realm/BFF, V1-V11, real operations deny-all, and financial code.
6. Execute safe tests; run provider tests only when the isolated environment and injected secrets are available.

Docker API unresponsiveness blocked container startup. No unrelated containers were stopped or restarted.
