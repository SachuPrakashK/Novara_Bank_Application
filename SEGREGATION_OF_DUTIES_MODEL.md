# Segregation of Duties Model

Status: **PROPOSED — PENDING APPROVAL**

## Candidate controls

| Future action | Maker | Checker / supervisor | Additional control |
| --- | --- | --- | --- |
| Sensitive request resolution or rejection | Assigned agent | Independent checker when policy classifies sensitive | Reason code, evidence, recent MFA |
| Cross-team reassignment | Agent/supervisor | Receiving supervisor for restricted queues | Queue-scope validation and immutable audit |
| Customer restriction recommendation | Agent | Authorized supervisor/checker | Dual control; no direct ledger change |
| Entitlement grant/change | Access administrator | Role/data owner | Ticket evidence, expiry, recent MFA |
| Emergency access activation | Requester/custodian | Security duty officer | Vault checkout, time limit, alert, review |
| Outbox replay | Platform operator | Independent service owner/checker | Idempotency assessment, incident reference, recent MFA |

## Proposed state machine

`DRAFT -> SUBMITTED_FOR_APPROVAL -> APPROVED | REJECTED -> EXECUTED | EXPIRED`, with `CANCELLED` allowed before approval. Maker identity must differ from checker identity; authorization is re-evaluated at approval and execution. Version checks prevent stale approval. Evidence references and decisions are append-only.

No approval engine or sensitive execution path is implemented.
