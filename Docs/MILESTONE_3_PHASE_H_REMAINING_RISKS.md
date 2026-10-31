# Phase H Remaining Risks

## Provider-blocked

- Real email, SMS, push and WhatsApp delivery and contact verification.
- Approved IdP password/MFA management URL and revoke-all-provider-sessions integration.
- Real authenticated browser acceptance without supplied credentials/MFA automation.

## Policy/operations-blocked

- Staff processing and status transitions for service requests.
- Legal approval for mutable regulated customer fields and mandatory-message opt-out rules.
- KYC/identity verification and account closure/freeze/mandate/nominee workflows.

## Production blockers

- Operational acceptance, accessibility/browser matrix testing, provider contracts, retention policies and monitoring/SLA sign-off.
- Deposit, statement and loan notification producers require approved durable outbox events before activation.

Phase I should not begin until Phase H operational ownership and provider decisions are approved. The codebase is technically ready for planning only, not production certification.

