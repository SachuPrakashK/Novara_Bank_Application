# Staff Real OIDC Acceptance Results

Provider execution status: **BLOCKED / NOT RUN**.

Two conditional tests are implemented to obtain a real Keycloak service token and verify provider discovery, published JWKS signature, exact issuer, expected audience, authorized-party client, subject presence, expiration, tamper rejection, and wrong-audience rejection. They were skipped because Docker could not start the disposable provider.

| Scenario | Status |
| --- | --- |
| Realm JSON and Compose model | **VERIFIED LOCALLY** |
| Real discovery/JWKS/token validation | **BLOCKED** |
| Invalid signature via tampered real token | **BLOCKED** |
| Wrong audience against real token | **BLOCKED** |
| Human subject stability/unmapped identity | **BLOCKED** |
| Customer/workforce cross-issuer provider test | **BLOCKED** |

Realm-template validation is not authentication acceptance. No signed token was fabricated.
