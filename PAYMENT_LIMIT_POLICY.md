# Payment limit policy

The initial policy supports bank per-payment and daily internal-transfer limits, optional customer/account overrides, and a customer-selected lower daily limit. The effective value is always the most restrictive applicable value.

Usage is completed principal plus fees during the Asia/Kolkata calendar day. Pending, failed, cancelled, and reversed payments do not consume usage. Confirmation serializes decisions per customer and performs usage calculation and posting in one database transaction, preventing competing debits from collectively exceeding the limit.

Customers may lower their selected limit immediately. An increase is rejected with `LIMIT_INCREASE_REQUIRES_REVIEW`; the unfinished provider-backed step-up continuation and bank-review workflow must be completed before increases are exposed.
