# Deposit Ledger Postings

Opening and RD installments debit the eligible customer funding-account liability and credit the contract-specific customer-deposit liability for an equal INR amount. `LedgerPostingService` validates balance, updates ledger balances, and creates immutable journals inside the same local transaction as the contract/occurrence.

Posting references are unique and tied to the deposit operation key. Interest accrual/capitalization, maturity repayment, closure, and renewal postings are blocked pending approved chart-of-accounts mappings and finance policy.
