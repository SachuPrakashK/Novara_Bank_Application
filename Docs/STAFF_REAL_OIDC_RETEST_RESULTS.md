# Staff Real OIDC Retest Results

Execution date: 2026-10-31.

Real-provider tests: **2 SKIPPED / 0 EXECUTED** because Docker is unavailable, the verifier secret is absent, and `NOVARA_WORKFORCE_REAL_PROVIDER_TESTS` is not enabled.

| Scenario | Result |
|---|---|
| Discovery and JWKS | BLOCKED |
| Real signature/issuer/audience/authorized client/expiry | BLOCKED |
| Tampered token and wrong audience | BLOCKED |
| Wrong issuer, not-before, invalid signature | BLOCKED |
| Human authorization-code/PKCE/state/nonce login | BLOCKED |

No token was fabricated, captured, logged, or stored. The configured image version is 26.0.8; the actual provider version is unavailable because no provider ran.
