# Deposit API Contracts

- `GET /api/v1/deposits/products?type=FIXED|RECURRING`
- `GET /api/v1/deposits`
- `GET /api/v1/deposits/{id}`
- `GET /api/v1/deposits/{id}/schedule`
- `POST /api/v1/deposits/fixed/reviews`
- `POST /api/v1/deposits/fixed/confirmations`
- `POST /api/v1/deposits/recurring/reviews`
- `POST /api/v1/deposits/recurring/confirmations`

Review/confirmation bodies contain product ID, amount in minor units, tenure months, funding-account public ID, and `CREDIT_LINKED_ACCOUNT`. Confirmations require a 16–100 character `Idempotency-Key`, authenticated ownership, CSRF, and recent MFA. Customer IDs are never accepted as authorization evidence.
