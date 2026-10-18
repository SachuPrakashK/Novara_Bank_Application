# OIDC integration test results

Verification date: 18 October 2026 (Asia/Calcutta).

| Status | Check | Evidence / result |
|---|---|---|
| PASS | Keycloak startup and realm import | `quay.io/keycloak/keycloak:26.0.8` ran on loopback port 8081; discovery returned issuer `http://localhost:8081/realms/novara`. |
| PASS | Client and redirect registration | Authorization request for `novara-bff` and the exact Spring callback returned the Keycloak login page, not a client/redirect error. |
| PASS | BFF connectivity | Real Spring process started against Keycloak and isolated PostgreSQL; health returned `UP`; Flyway V1–V3 applied. |
| PASS | Authorization request protection | Live BFF redirect contained nonempty `state`, OIDC `nonce`, `code_challenge`, and `code_challenge_method=S256`. |
| PASS | MFA enrollment enforcement | Realm required action reports `CONFIGURE_TOTP enabled=true, defaultAction=true`; temporary user's valid password proceeded to the TOTP enrollment page. |
| PASS | Provider abuse configuration | Admin API reported brute-force protection enabled, failure factor 5, TOTP/HMAC-SHA256/6 digits, and WebAuthn user verification `required`. |
| BLOCKED | Completed login and callback | TOTP enrollment/challenge was not completed by an automated browser in this pass. No authenticated BFF session is claimed. |
| BLOCKED | Key rotation | No controlled signing-key rotation was executed. Runtime JWKS lookup supports rotation, but that is implementation evidence only. |
| NOT IMPLEMENTED | IdP global logout | Application logout invalidates the local BFF session only. |

The temporary test identity contained no production data. This test does not certify the identity provider for production.
