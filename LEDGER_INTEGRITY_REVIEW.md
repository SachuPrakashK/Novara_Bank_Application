# Ledger integrity review

Money uses signed 64-bit integer minor units and INR currency checks. Application arithmetic uses `Math.addExact`; PostgreSQL requires positive line amounts and a single currency. A deferred constraint trigger rejects zero or unbalanced posted journals.

Posted journals and lines are immutable. Corrections create opposite-direction journals linked by the unique `reversal_of_journal_id`; originals are never changed. Reversal attempts serialize on a transaction advisory lock, recheck for an existing reversal, and retain the unique database constraint as defense in depth.

Transfers lock both bank-account and projection rows in opaque-ID order. Holds acquire the account lock as well, so hold placement and spending cannot race. Available funds are:

`ledger balance - active unexpired holds - minimum balance + explicit overdraft limit`

Defaults introduce no new product rule: minimum balance and overdraft are zero unless configured. Holds do not rewrite ledger authority and are independently visible to the available-balance projection. Reconciliation continues to recompute ledger balances solely from immutable posted journal lines.

