# API contracts

All routes require a valid OIDC access token except health probes. Identifiers are opaque UUIDs. Error responses use Problem Details and include a safe `code` and correlation ID.

## Customer and accounts

- `GET /api/v1/me`
- `GET /api/v1/accounts`
- `GET /api/v1/accounts/{accountId}`
- `GET /api/v1/accounts/{accountId}/balance`
- `GET /api/v1/accounts/{accountId}/transactions?limit=50`

Account lookups always include the authenticated identity subject in the ownership query. Missing and unauthorized accounts deliberately produce the same response.

## Beneficiaries

- `GET /api/v1/beneficiaries?query=&status=&favorite=`
- `GET /api/v1/beneficiaries/{beneficiaryId}`
- `POST /api/v1/beneficiaries` requires a 16–100 character `Idempotency-Key`
- `PATCH /api/v1/beneficiaries/{beneficiaryId}` permits display-name and nickname changes only
- `PUT /api/v1/beneficiaries/{beneficiaryId}/favorite`
- `DELETE /api/v1/beneficiaries/{beneficiaryId}` requires recent identity-provider MFA and performs soft deactivation

Creation returns `PENDING_VERIFICATION`. Account identifiers are masked in responses. Verification and cooling are backend-owned; submission is never proof of verification.

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
## Advanced internal payments

- `POST /api/v1/payments/intents` creates an expiring, server-authorized review.
- `POST /api/v1/payments/{paymentId}/confirm` confirms with `Idempotency-Key`; financial details come from the locked intent.
- `GET /api/v1/payments` provides ownership-scoped pagination/filtering; `GET /api/v1/payments/{paymentId}` returns detail and timeline.
- `GET /api/v1/payments/{paymentId}/receipt` returns a no-store authoritative receipt representation only for completed payments.
- `GET /api/v1/payment-limits` and `PATCH /api/v1/payment-limits/customer-preferences` expose usage and lowering-only customer controls.
- `GET|POST /api/v1/payment-schedules`, `GET /api/v1/payment-schedules/{id}`, and `POST /api/v1/payment-schedules/{id}/cancel` manage durable schedules.

Browser mutations require CSRF. UPI, IMPS, NEFT, and RTGS are rejected as unavailable.
