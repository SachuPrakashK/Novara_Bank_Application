# Beneficiary management design

## API

- `GET /api/v1/beneficiaries` supports `query`, `status`, and `favorite` filters.
- `GET /api/v1/beneficiaries/{id}` returns one ownership-scoped resource.
- `POST /api/v1/beneficiaries` requires `Idempotency-Key` and creates a pending resource.
- `PATCH /api/v1/beneficiaries/{id}` changes permitted display fields only.
- `PUT /api/v1/beneficiaries/{id}/favorite` changes favorite state.
- `DELETE /api/v1/beneficiaries/{id}` performs MFA-gated soft deactivation.

All identifiers exposed by the API are opaque UUIDs. Missing and unauthorized resources both return `BENEFICIARY_NOT_FOUND`. Validation and domain failures use the existing Problem Details contract.

## Verification and cooling

Form submission never verifies a destination. It creates a pending verification attempt. A future provider/operations adapter must authenticate its callback or command, bind the result to the attempt, prevent replay, and call the transactional verification boundary. Success records `verified_at` and a server-owned cooling deadline; failure restricts the beneficiary. Both outcomes create audit events.

## Sensitive-data handling

- Raw account identifiers are not stored, returned, or written to audit records.
- A keyed HMAC enables duplicate detection without a reversible account value.
- Only the final four characters are retained for display.
- Routing codes are normalized to uppercase and constrained by the database.
- Key rotation requires a planned dual-key migration because the fingerprint participates in duplicate detection.

## User experience

The page provides responsive search, status filtering, loading skeletons, empty state, add, detail expansion, permitted edits, favorite controls, deactivation, success/error feedback, masked identifiers, and explicit verification/cooling labels. External methods are explicitly described as sandbox boundaries. The page reuses Novara design tokens, dark theme, focus rings, reduced-motion handling, and lazy loading.

## Deliberate exclusions

Destination account edits are excluded because they constitute a new high-risk beneficiary and require a fresh verification lifecycle. Hard deletion is excluded to preserve audit history. Payment execution and recent-beneficiary ranking by actual transfers belong to Phase C.

