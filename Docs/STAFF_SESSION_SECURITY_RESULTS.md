# Staff Session Security Results

| Behavior | Evidence |
| --- | --- |
| Session fixation changes ID | Existing Spring configuration; **VERIFIED WITH MOCKS/config review** |
| Secure/HttpOnly/SameSite cookies | Existing configuration; local secure override only; **VERIFIED WITH MOCKS/config review** |
| CSRF required for browser mutations | Existing integration tests; **VERIFIED WITH MOCKS** |
| Idle and absolute expiry | Existing session/filter tests; **VERIFIED WITH MOCKS** |
| Logout invalidation | Existing integration test; **VERIFIED WITH MOCKS** |
| Concurrent session maximum | Configured at three; real-provider behavior **NOT RUN** |
| Token refresh, IdP logout/revocation, role removal | **BLOCKED** for real workforce provider |
| Bearer/browser operations denial | **VERIFIED WITH MOCKS** |

No workforce browser session chain was added. A future chain must be isolated by issuer/client, namespace, profile, and explicit deny-by-default rules.

## Dual-priority rerun — 2026-11-02

Genuine workforce login, logout, refresh, expiry, suspension, invalidation, revocation, reauthentication, and residual-token behavior were **NOT RUN / BLOCKED**. Required secrets and approved human identities/factors were absent, and Docker later returned HTTP 500. Prior short-lived non-human token expiry evidence remains valid but is not human session evidence.
