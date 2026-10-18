# Session security verification

| Status | Control | Actual result |
|---|---|---|
| PASS | Cookie attributes | Live BFF emitted `NOVARA_SESSION` with `HttpOnly`, path `/`, and `SameSite=Lax`. Production defaults `Secure=true`; local verification explicitly disabled Secure over HTTP. |
| PASS | Server-side storage | V3 created Spring Session JDBC tables and clean PostgreSQL migration passed. |
| PASS | Fixation/concurrency configuration | Spring Security uses `changeSessionId()` and maximum three sessions. Source/configuration verified; successful login rotation E2E remains blocked. |
| PASS | Idle timeout | Server timeout is 15 minutes. |
| PASS | Absolute timeout | Eight-hour filter now invalidates, clears authentication, returns 401, and stops the current request; unit-tested. |
| PASS | Local logout CSRF/invalidation | MockMvc verifies missing-CSRF rejection and valid-CSRF 204 through Spring logout. |
| PASS | Other-session termination | Repository deletion is restricted to sessions indexed under the authenticated principal and cannot target the current session. |
| BLOCKED | Multi-replica invalidation | Only one BFF instance was run. |
| NOT IMPLEMENTED | Global IdP logout | Local session logout does not terminate Keycloak SSO. |

Policy rationale: 15-minute inactivity limits unattended-browser exposure; eight hours bounds absolute lifetime; three concurrent sessions supports common multi-device use while limiting persistence.
