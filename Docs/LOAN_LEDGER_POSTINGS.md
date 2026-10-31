# Loan Ledger Postings

## Current decision: no postings

Quotes and submitted applications have no financial effect. They create no journal, journal line, hold, balance mutation, loan account, disbursement, or repayment record. Integration tests explicitly verify zero journals and zero loan accounts after the Phase F workflow.

Implementation of postings requires approved mappings for loan receivable principal, customer cash settlement, accrued interest, interest income, fees, suspense, reversals, write-offs, and repayment allocation priority. It also requires atomic failure and idempotency rules for every money-moving operation. Until those policies are supplied, every such operation remains unavailable by design.
