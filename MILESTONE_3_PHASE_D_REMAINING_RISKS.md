# Milestone 3 Phase D Remaining Risks

## High

- A production card-processor adapter, provider security review, reconciliation process, and operational runbooks do not exist. Non-sandbox mode therefore fails closed. Real card operations must not be enabled.

## Medium

- Processor timeout and ambiguous-outcome recovery need an outbox/reconciliation worker before production integration.
- Service requests stop at durable issuer-review submission; there is no operations queue, fulfillment, delivery tracking, or customer notification.
- Card activity is read-only and capped at the latest 100 records; server pagination/filtering and processor ingestion remain future work.
- Limit editing is supported by the API, while this phase's customer view exposes channel switches and read-only limit summaries. A reviewed limit-editing experience is still required.

## Low

- The SVG card preview is intentionally illustrative and not a network-specific reproduction.
- Device/browser visual regression coverage should be expanded beyond build and component checks.

Phase E has not started. Phase D must not be described as production-ready until the High risk is closed and the Medium risks are accepted or remediated.
