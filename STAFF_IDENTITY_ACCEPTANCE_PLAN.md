# Staff Identity Acceptance Plan

Status: **BLOCKED** pending controlled identities, real MFA mechanisms, vault-backed secrets, and IAM/Security participation.

| Test area | Required evidence |
| --- | --- |
| Issuer/audience/subject | Reject wrong issuer/audience/signature; prove subject stability across login, rename, refresh, and lifecycle events |
| Identity separation | Customer token/session cannot become staff; staff client/realm cannot resolve as customer accidentally |
| MFA/freshness | Real configured factor produces approved assurance claims; stale/missing evidence rejected; no bypass |
| Session | Fixation protection, secure cookie, idle/absolute expiry, concurrent-session rule, logout and provider/session revocation |
| Lifecycle | Activation, suspension, leaver disablement, role removal, refresh-token behavior, active-session termination and reconciliation |
| Authorization abuse | Forged roles, privilege escalation, cross-customer/queue/organization access, IDOR and denied API paths |
| Browser security | CSRF success/failure, SameSite/Secure behavior, origin/CORS, post-logout access |

Use disposable controlled customer/staff/privileged/emergency identities, authentic MFA test devices or approved automation, isolated test tenant, synchronized clocks, and secrets delivered from the test vault. Capture provider configuration version, request/result, audit evidence, and reviewer sign-off. MockMvc and demo sessions are useful regressions but are not provider acceptance evidence.
