# Scheduled and recurring payment design

Supported recurrence is one-time, weekly, and monthly. Timestamps are stored as instants with an IANA timezone. Weekly execution preserves local time. Monthly execution clamps to the last valid day of shorter months. Java timezone rules resolve daylight-saving changes.

Workers claim eligible schedules with PostgreSQL `FOR UPDATE SKIP LOCKED` and a two-minute lease. The `(schedule_id, occurrence_number)` and `(schedule_id, scheduled_for)` constraints prevent duplicate occurrences; a stable occurrence-derived idempotency key prevents duplicate ledger posting.

Funds are not reserved at schedule creation. Customer, accounts, beneficiary verification/cooling/resolution, available funds, and limits are rechecked at execution. Failures and high-risk step-up dependencies move the schedule to `REQUIRES_REVIEW`. Cancellation is allowed only before completed execution. Pausing/resuming and administrative recovery UI remain follow-up work.
