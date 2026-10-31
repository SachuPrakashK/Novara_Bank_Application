# Milestone 3 — Phase G Plan

Phase G replaces the limited client-filtered transaction view with authoritative posted-ledger statements and receipts. Delivery order: inspect ledger semantics; define cutoff/direction/balance rules; implement server filtering and pagination; persist statement generation metadata; render PDF/CSV and receipts; build the lazy Angular workspace; verify security, financial reconciliation, exports, and regressions.

Only journal-backed events are supported. External settlement, unposted card activity, loan quotations/applications, unposted interest, and future deposit payouts remain excluded.
