# Statement API Contracts

| Method | Route | Purpose |
|---|---|---|
| GET | `/api/v1/statements/accounts` | Owned statement-capable accounts |
| GET | `/api/v1/statements/transactions` | Server-filtered, stably paged posted activity and full-range summary |
| GET | `/api/v1/statements/summary` | Full-period authoritative balances |
| POST | `/api/v1/statements/generations` | Create immutable generation metadata; CSRF and `Idempotency-Key` required |
| GET | `/api/v1/statements/{id}` | Read an owned generated snapshot |
| GET | `/api/v1/statements/{id}/pdf` | Download statement PDF |
| GET | `/api/v1/statements/{id}/csv` | Download safe UTF-8 CSV |
| GET | `/api/v1/receipts/{journalId}` | Read an owned posted receipt |
| GET | `/api/v1/receipts/{journalId}/pdf` | Download receipt PDF |

Transaction parameters include account, inclusive dates, direction, type, search, sort, page, and size. Document responses use `no-store`, `no-cache`, `nosniff`, explicit content type, length, and attachment disposition without account identifiers.

Generation keys use 16–100 letters, digits, underscores, or hyphens. Same-key retries return the original generation; reuse for a different account or period is rejected. A transaction-scoped advisory lock and unique customer/key index make concurrent duplicates safe.
