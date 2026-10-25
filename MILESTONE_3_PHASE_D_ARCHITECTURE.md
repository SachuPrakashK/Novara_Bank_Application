# Milestone 3 Phase D Architecture

## Module boundary

`com.novara.banking.card` is a modular-monolith slice containing the controller, application service, commands/views, and the `CardProcessor` outbound port. Persistence uses the shared PostgreSQL database through Spring `JdbcClient`; schema ownership begins with Flyway V6.

Authenticated issuer and subject claims are resolved to an active customer by `CurrentIdentity`. Every card read or mutation includes that customer key. Unknown and foreign card identifiers both return the same not-found response.

## Data model

- `card_products`: issuer-defined product metadata.
- `cards`: customer-owned masked card record and lifecycle state.
- `card_controls`: channel switches and minor-unit limits with a version.
- `card_control_requests`: idempotency and processor outcome record.
- `card_status_events`: append-only lifecycle history.
- `card_transactions`: processor-supplied masked activity.
- `card_service_requests`: issuer-review workflow; submission is not fulfillment.

Card numbers, CVVs, PINs, and magnetic-stripe data are never stored or returned.

## Request flow

Mutating requests pass authentication, CSRF, ownership, validation, state-transition, and (where applicable) recent-MFA checks. Freeze/unfreeze and controls call `CardProcessor`; local state changes only after acknowledgement. Database locking serializes concurrent commands and idempotency keys prevent duplicate effects.

The Angular module is lazy-loaded. `CARDS_API` selects the HTTP adapter normally and the explicitly fictional in-memory adapter in development mode.
