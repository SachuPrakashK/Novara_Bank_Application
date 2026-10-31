# Statement Balance Rules

- Banking timezone: `Asia/Kolkata`.
- Date intervals are inclusive local calendar dates; posting timestamps remain explicit instants.
- Maximum history is six months and maximum page size is 100. Invalid ranges are rejected, never truncated.
- Customer savings/current accounts map to liability ledgers: ledger `CREDIT` is customer money-in and ledger `DEBIT` is customer money-out.
- Reversals remain visible. The original `REVERSED` journal and its separately posted reversing journal are not deleted or hidden.
- Closing balance at the cutoff equals the current authoritative projection less all net postings after the period end through the cutoff.
- Opening = closing − interval credits + interval debits.
- Running balances are calculated across the complete ordered interval before filtering or pagination.
- Stable order is posting timestamp plus journal-line ID.
- Generated statements persist the maximum committed journal-line ID visible under PostgreSQL `REPEATABLE READ`; that ID, rather than wall-clock time alone, is the authoritative accounting cutoff.

The ledger has no distinct backdated value-date field. Phase G reports the banking-local posting date as value date and does not invent backdating rules.
