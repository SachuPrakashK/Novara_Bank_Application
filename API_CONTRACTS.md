# API contracts

All routes require a valid OIDC access token except health probes. Identifiers are opaque UUIDs. Error responses use Problem Details and include a safe `code` and correlation ID.

## Customer and accounts

- `GET /api/v1/me`
- `GET /api/v1/accounts`
- `GET /api/v1/accounts/{accountId}`
- `GET /api/v1/accounts/{accountId}/balance`
- `GET /api/v1/accounts/{accountId}/transactions?limit=50`

Account lookups always include the authenticated identity subject in the ownership query. Missing and unauthorized accounts deliberately produce the same response.

## Internal transfers

`POST /api/v1/transfers/internal` requires an unpredictable `Idempotency-Key` header of 16–100 URL-safe characters.

```json
{
  "sourceAccountId": "opaque-uuid",
  "destinationAccountId": "opaque-uuid",
  "amountMinor": 125000,
  "currency": "INR",
  "note": "Fictional transfer"
}
```

- `GET /api/v1/transfers/{transferId}`
- `GET /api/v1/transfers/by-idempotency-key/{key}`

Idempotency lookup is scoped to the authenticated principal and `INTERNAL_TRANSFER` operation. The same textual key may safely be used by different principals; it never permits cross-customer retrieval.

Retrying the same request/key returns the original resource. Reusing a key with a different canonical request hash returns `409 IDEMPOTENCY_KEY_REUSED`.

## Operations

`POST /api/v1/operations/reconciliation` requires `operations.reconcile` scope and never repairs records.
