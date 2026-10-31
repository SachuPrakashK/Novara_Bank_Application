# Staff MFA Acceptance Results

## Mock evidence

- Missing MFA evidence is denied: **VERIFIED WITH MOCKS**.
- MFA evidence older than five minutes is denied: **VERIFIED WITH MOCKS**.
- Recent MFA evidence participates in authorization but is never sufficient alone: **VERIFIED WITH MOCKS**.

## Real provider evidence

Successful login, real TOTP/WebAuthn enrollment, incorrect-factor rejection, authentic assurance claims, refresh, suspension, revocation, and post-revocation denial are **BLOCKED**. No controlled staff identities, approved MFA devices/automation, or vault-delivered client secret were available. No claim was fabricated and the customer demo MFA is not counted as provider evidence.
