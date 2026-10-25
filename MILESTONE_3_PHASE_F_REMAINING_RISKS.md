# Phase F Remaining Risks

- Product and credit governance: sandbox terms must not be promoted without approved pricing, eligibility, underwriting, disclosures, and adverse-action policy.
- Accounting: no approved ledger mappings exist for disbursement, accrual, payment, fee, reversal, suspense, write-off, or settlement.
- Servicing: repayment allocation, holiday calendars, delinquency, grace periods, prepayment, foreclosure, restructuring, and collections are undefined.
- Authentication: automated tests validate recent-MFA enforcement, but a production identity-provider browser ceremony still needs environment-level acceptance testing.
- Calendar behavior: quote schedules use calendar-month increments without business-day adjustment.
- Scale: schedule insertion is intentionally simple for the sandbox limits and should be load-tested before production use.

Phase G is not unconditionally ready. Read-only/application capabilities are complete, but any loan money movement is blocked until the above policies are approved and encoded as testable requirements.
