# Customer Profile Rules

- Customer identity is always resolved server-side from OIDC issuer and subject.
- Legal name, registered email/mobile, address, verification flags and customer-since date are read-only.
- Only `preferredDisplayName` is editable in Phase H.
- Updates require CSRF and the current optimistic `version`.
- Missing values are shown as unavailable and are never fabricated.
- Internal customer IDs and identity-provider subjects are not returned.
- Contact changes require a service request; no OTP or verification success is simulated.

