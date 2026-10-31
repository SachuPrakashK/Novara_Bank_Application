# Security Center Rules

- Session inventory and termination use the real Spring Session repository.
- MFA is confirmed only when the IdP `amr` claim reports MFA/OTP; otherwise the UI says it was not reported.
- Last login comes from successful OIDC login audit events.
- Password, MFA, passkey and recovery management remain at the identity provider.
- No password, OTP, recovery code or IdP secret is stored by Novara.
- No password-management link or global revoke action is shown because an approved IdP integration is unavailable.

