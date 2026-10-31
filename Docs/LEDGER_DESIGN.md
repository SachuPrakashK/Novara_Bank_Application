# Ledger design

Novara uses integer INR minor units (`bigint`) and never floating-point values. Customer deposit accounts map one-to-one to liability ledger accounts: credits increase the customer balance and debits decrease it.

Each posting has a unique batch reference and journal reference. A journal begins `PENDING`, receives ordered lines, updates its balance projections, and becomes `POSTED` inside one transaction. Application validation uses overflow-checked arithmetic. A deferred PostgreSQL constraint trigger independently rejects a posted journal whose debit and credit totals differ, have no value, or contain inconsistent currency.

Posted journal lines cannot be updated or deleted. Corrections create a new journal with opposite directions and a unique `reversal_of_journal_id`; the original record remains intact. Projection updates and journal writes share a transaction. Reconciliation independently recomputes balances from posted lines and reports differences without modifying authority.

Reversal attempts serialize with a PostgreSQL transaction advisory lock and then recheck the unique reversal relationship, preventing both sequential and concurrent duplicate reversal. Available balance is separate from ledger authority: active unexpired holds and an explicitly configured minimum balance reduce spendable funds, while an explicitly configured overdraft limit can increase it. All three product fields default to zero-impact behavior.

The database trigger is a final integrity backstop, not a replacement for domain validation. Direct database access must be restricted so only the migration principal can alter ledger functions or triggers.
