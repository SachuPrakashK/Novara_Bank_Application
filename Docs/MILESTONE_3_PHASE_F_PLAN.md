# Milestone 3 — Phase F Plan

## Delivered scope

- Versioned fictional sandbox loan products.
- Server-authoritative fixed-rate reducing-balance EMI quotes.
- Durable quote and installment schedule snapshots.
- Customer-owned, idempotent application submission with recent MFA at the HTTP boundary.
- Application status tracking and a responsive customer UI.

## Policy gate

No approved loan accounting mappings, approval policy, disbursement controls, repayment allocation order, holiday calendar, delinquency policy, prepayment rules, or foreclosure rules were supplied. Phase F therefore stops before credit decisions and all financial postings. No synthetic accounting behavior has been invented.

## Completion order

1. V8 product, quote, application, status-event, and reserved account schema.
2. Deterministic EMI engine and ownership-secured APIs.
3. Angular quote/application experience.
4. Unit, database integration, security, and production-build verification.
5. Policy-boundary documentation and Phase G readiness review.
