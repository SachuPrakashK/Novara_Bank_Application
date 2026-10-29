# Outbox Recovery Design

Status: **PROPOSED — RECOVERY ACTIONS BLOCKED**

## Actual capability

`outbox_events` contains event/aggregate identifiers, type, JSON payload, occurrence time, and nullable publication time. It can support pending count, published count, and oldest-pending age. A trigger prevents deletion. It cannot truthfully provide failures, attempts, error class, next retry, quarantine, dead-letter state, consumer health, or replay history.

## Future design

Add approved attempt records rather than fake state: attempt number/time, consumer, outcome, classified error code, privacy-safe diagnostic, next retry, and correlation. Quarantine/dead-letter decisions require policy, ownership, alerting, and runbooks. Consumer health comes from telemetry, not inferred database state.

Replay requires incident/change reference, eligible terminal state, idempotency/side-effect assessment, maker-checker authorization, recent MFA, bounded selection, immutable operator audit, and post-replay verification. Original events are never deleted or silently rewritten. Payload exposure is minimized and redacted. Manual replay, deletion, and privileged recovery remain disabled.
