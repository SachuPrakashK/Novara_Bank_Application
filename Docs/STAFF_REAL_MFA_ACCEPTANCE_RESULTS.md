# Staff Real MFA Acceptance Results

Status: **BLOCKED — 0 real MFA tests executed**.

The workforce realm configures genuine Keycloak TOTP as a default required action. Configuration alone is not evidence that MFA occurred. No user, TOTP seed, OTP, passkey, or recovery code was created or logged.

Successful enrollment/login, invalid and missing factor rejection, `acr`/`amr` interpretation, authentication freshness, expired assurance, step-up, and suspended-user authentication remain blocked pending IAM-controlled identities, an approved MFA mechanism, and sanitized evidence capture. TOTP, if approved, must be documented as lower assurance than phishing-resistant WebAuthn.
