# Payment lifecycle

Legal progression is `PENDING_AUTHORIZATION -> PROCESSING -> COMPLETED`. Expired or rejected work may become `FAILED`; scheduled work begins as `SCHEDULED`; assurance or operational exceptions become `REQUIRES_REVIEW`; unexecuted instructions may become `CANCELLED`.

A completed payment cannot transition backward. Correction must use the existing controlled reversal/compensating-posting boundary. Acceptance, authorization, journal posting, and notification are separate facts. An HTTP timeout is `UNKNOWN` to the browser and triggers status lookup using the existing identifier; it is never interpreted as financial failure or retried with a new logical key.

The server intent fixes source, resolved destination, principal, fee, total debit, currency, method, description, expiry, and step-up requirement. Confirmation supplies no editable financial details.
