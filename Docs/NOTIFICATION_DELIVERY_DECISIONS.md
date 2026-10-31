# Notification Delivery Decisions

| Channel  | Provider and consent                                                              | Protection/failure requirements                                                           | Status and ownership                                   |
| -------- | --------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- | ------------------------------------------------------ |
| In-app   | Internal PostgreSQL/outbox; optional categories with mandatory security separated | Minimal content, owned reads, idempotent source IDs; retry by replaying the durable event | Implemented sandbox; Platform Operations owns backlog  |
| Email    | Approved transactional provider; verified address and consent/policy              | Regional processing, bounce/suppression, bounded retry, provider ID and delivery audit    | BLOCKED; Procurement/Product/Privacy; per-message cost |
| SMS      | Approved regulated provider; verified number, templates, DND/consent policy       | No account identifiers, receipts, bounded retry, carrier-failure quarantine               | BLOCKED; Product/Privacy; regional cost and limits     |
| Push     | Approved device-token provider and enrollment                                     | Token rotation/revocation, minimal lock-screen content, retry expiry                      | BLOCKED; Mobile/Platform ownership                     |
| WhatsApp | Approved business provider/templates and explicit opt-in                          | Minimal data, state audit, bounded retry and opt-out                                      | BLOCKED; template/conversation pricing                 |

External success must never be inferred from enqueueing. Provider message IDs, attempts, terminal state and privacy-safe error codes are required before enabling any external channel.
