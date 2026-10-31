# Audit Explorer Rules

The demonstration uses privacy-safe synthetic events with action, actor category, outcome, correlation ID and time. Real audit access is deny-all.

Future access requires explicit `AUDIT_EVENTS_READ`, purpose limitation, bounded dates/pages, allowlisted sorts, access auditing and payload masking. Tokens, passwords, OTPs, recovery codes, session secrets, government IDs and full financial identifiers must never be returned. Audit records remain append-only.
