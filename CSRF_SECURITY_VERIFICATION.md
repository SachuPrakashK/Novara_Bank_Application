# CSRF security verification

| Status | Test | Actual result |
|---|---|---|
| PASS | POST without token | Rejected 403; no transfer row posted. |
| PASS | Invalid token | Rejected 403; no transfer row posted. |
| PASS | PUT/PATCH/DELETE without token | Each rejected 403 before endpoint dispatch. |
| PASS | Valid token | Authenticated mapped-customer transfer returned 201 and posted exactly once. |
| PASS | Malicious origin | Authenticated request from `https://evil.example` without CSRF proof was rejected 403 and received no CORS allow-origin header. |
| PASS | Angular transport | Angular XSRF names match backend cookie/header and API interceptor uses credentials. |
| PASS | Cookie behavior | Live BFF emitted browser-readable `NOVARA-XSRF-TOKEN`, path `/`, `SameSite=Lax`; production defaults Secure. It is a CSRF secret, not an authentication token. |
| NOT APPLICABLE | Independently expired CSRF token | Tokens have session lifetime rather than an independent expiry; invalidated/expired sessions make the token unusable. |

CSRF remains disabled only on the stateless bearer-token chain. Selecting it requires a valid bearer credential that the BFF never exposes to Angular.
