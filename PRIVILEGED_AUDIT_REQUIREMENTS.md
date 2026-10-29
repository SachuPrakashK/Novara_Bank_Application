# Privileged Audit Requirements

Status: **PROPOSED**. Existing `audit_events` rows are append-only through a database trigger, but externally verified tamper evidence and the full schema below are not implemented.

Required events: staff login/logout/failure, denied access, customer search/view, account view, request claim/reassignment/transition, entitlement change, audit query/export attempt, outbox recovery, configuration view/change, and emergency-access lifecycle.

Required fields: event ID/version, occurred/recorded timestamps, immutable actor identity reference and actor type, session/client, action, resource type/opaque reference, business purpose/case, organizational scope, outcome/reason code, correlation/trace ID, authorization decision and policy version, assurance/freshness, maker/checker references, and retention classification.

Never record passwords, OTPs, recovery codes, tokens, cookies/session secrets, full government IDs, full account/card identifiers, or unrestricted payloads. Access is least-privilege, search is bounded and itself audited, exports default-denied, clocks are synchronized, and records are shipped to restricted append-only/tamper-evident storage after approval. Retention, legal hold, monitoring rules, and evidence verification are pending Security, Compliance, Legal, and Privacy approval.
