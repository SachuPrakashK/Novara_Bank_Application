# Identity Provider Acceptance

Status: **BLOCKED**.

- Missing dependency: authorized Keycloak customer identities, staff identity assignments, repeatable MFA automation, and an approved test-secret mechanism.
- Responsible team: Identity and Access Management with QA Security.
- Required next action: provision disposable controlled identities, configure authentic MFA, document assurance claims, and supply secrets through the test vault.
- Sandbox impact: mocked browser flows may run and are explicitly non-provider evidence.
- Production impact: login/logout, refresh, expiry, recent MFA, cookies, session termination and post-logout access remain unaccepted.

MockMvc OIDC tests and Angular demo sessions are not real provider acceptance. MFA will not be bypassed, and credentials will not be committed.
