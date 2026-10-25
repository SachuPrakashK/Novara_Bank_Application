# Phase H Architecture

Phase H remains inside the Java 21 modular monolith and Angular standalone client. Controllers derive the customer from the authenticated issuer/subject. PostgreSQL owns profile versions, request lifecycle, notification read state, and preferences.

Committed `InternalTransferCompleted` outbox events are projected into `customer_notifications`. `(customer_id, source_event_id)` makes retries idempotent. Service-request submission and its notification commit in one transaction. No new financial source of truth was introduced.

V11 is forward-only and leaves V1–V10 unchanged. Original journals remain POSTED; reversal is a separate POSTED journal referencing the original.

