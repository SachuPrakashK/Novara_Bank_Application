# Loan API Contracts

All routes require an authenticated customer session.

| Method | Route | Purpose |
|---|---|---|
| `GET` | `/api/v1/loans/products` | Active sandbox products and versioned terms |
| `POST` | `/api/v1/loans/quotes` | Validate terms and persist an authoritative quote/schedule |
| `GET` | `/api/v1/loans/quotes/{id}/schedule` | Read the customer's illustrative schedule |
| `POST` | `/api/v1/loans/applications` | Submit an application; requires CSRF, recent MFA, and `Idempotency-Key` |
| `GET` | `/api/v1/loans/applications` | List the customer's applications |
| `GET` | `/api/v1/loans/applications/{id}` | Read one owned application |

Quote input is `productId`, `principalMinor`, and `tenureMonths`. Application input is `quoteId`, an allowed `purposeCategory`, and a true `customerDeclaration`. Application idempotency keys allow 16–100 letters, digits, underscores, or hyphens. A key reused with different content is rejected. Quotes expire after 30 minutes and can be used once.

There are intentionally no approval, account, disbursement, repayment, prepayment, foreclosure, delinquency, or write-off endpoints.
