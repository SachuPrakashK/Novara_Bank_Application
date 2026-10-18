# Security review

## Findings fixed

### High — PKCE was not forced by the confidential Spring client

- Affected: `SecurityConfig.java`
- Root cause: Keycloak required S256, but Spring's confidential client did not explicitly add PKCE.
- Fix: custom authorization request resolver applies `withPkce()`.
- Verification: live BFF redirect contained `code_challenge_method=S256` and a challenge.
- Remaining risk: completed callback still requires E2E coverage.

### High — absolute-expired session could continue the current request

- Affected: `AbsoluteSessionLifetimeFilter.java`
- Root cause: session was invalidated but the filter chain continued.
- Fix: clear security context, return 401, stop the chain.
- Verification: focused unit test.

### High — high-value transfer lacked step-up enforcement

- Affected: `TransferController.java`, `StepUpAuthorizationService.java`
- Root cause: no backend assurance/age check.
- Fix: threshold gate validates recent `auth_time` and provider `amr`; otherwise `STEP_UP_REQUIRED` before posting.
- Verification: controller rejection plus focused accepted/stale/single-factor tests.

### Medium — unsafe production defaults

- Affected: `application.yml`, `.env.example`
- Root cause: fallback client secret and non-Secure cookies.
- Fix: mandatory client secret; session and CSRF cookies default Secure, with explicit local HTTP overrides.
- Verification: configuration inspection and live local override response.

### Medium — incomplete CORS method allowlist

- Affected: `SecurityConfig.java`
- Fix: allow required PUT/PATCH/DELETE while retaining one explicit origin and credentials policy.
- Verification: untrusted-origin request rejected.

## OWASP assessment

BOLA is mitigated by issuer/subject mapping and owner joins; broken function authorization by explicit operations rules; CSRF by session-chain tokens; fixation by session ID change; open redirects by allowlist; sensitive token disclosure by server-side OAuth storage; unsafe errors by centralized responses. Distributed rate limiting, target TLS/proxy controls, external audit retention, and independent penetration testing remain deployment obligations.

This is not evidence of RBI, PCI DSS, or other regulatory compliance.
