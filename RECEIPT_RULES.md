# Receipt Rules

A receipt is available only when the authenticated customer owns a bank account represented by a line in a committed `POSTED` or `REVERSED` journal. Receipt identifiers are non-sequential journal UUIDs and failed ownership checks return the same not-found response.

Receipts contain the journal reference, posting time, safe ledger description, amount, currency, status, transaction/reversal type, and the participating owned account mask. They do not invent settlement IDs, signatures, external rails, counterparties, or certifications.

Pending/failed payment intents, unexecuted schedules, loan quotes/applications, card controls, unposted interest, and unsupported settlement events are ineligible.
