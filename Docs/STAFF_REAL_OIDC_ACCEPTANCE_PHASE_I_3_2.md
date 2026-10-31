# Staff Real OIDC Acceptance — Phase I.3.2

Five tests passed against running Keycloak 26.0.8.

| Scenario | Status |
|---|---|
| Live discovery and published nonempty JWKS | VERIFIED WITH REAL KEYCLOAK |
| Genuine signature, issuer, audience, authorized client, expiry, and subject | VERIFIED WITH REAL KEYCLOAK |
| Mutated/invalid signature rejection | VERIFIED WITH REAL KEYCLOAK |
| Wrong audience and wrong issuer rejection | VERIFIED WITH REAL KEYCLOAK |
| 20-second token rejected after expiry using zero-skew acceptance validation | VERIFIED WITH REAL KEYCLOAK |
| Future not-before case | BLOCKED; no suitable provider token emitted |
| Human subject stability/unmapped human subject | BLOCKED; mock policy coverage retained |

This is service-token cryptographic evidence, not human authentication acceptance.
