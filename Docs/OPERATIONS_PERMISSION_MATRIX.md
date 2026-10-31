# Operations Permission Matrix

Status: **PROPOSED — PENDING APPROVAL**. Every permission also requires trusted staff identity, active assignment, organizational scope, business purpose, and server-side authorization.

| Area / permission | Purpose and allowed resources | Proposed role | Proposed scope | Assurance | Required audit | Classification | Approval owner |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Dashboard / `OPERATIONS_DASHBOARD_READ` | Aggregated scoped workload only | Agent, Supervisor | assigned queues/org | Staff MFA | dashboard view | Internal | Operations Security |
| Requests / `SERVICE_REQUEST_READ` | Requests in assigned queue | Agent, Supervisor | queue | Staff MFA | search/view | Confidential | Customer Operations + Privacy |
| Requests / `SERVICE_REQUEST_ASSIGN` | Claim/reassign eligible request | Agent, Supervisor | queue/team | Staff MFA; recent auth for override | assignment | Confidential | Customer Operations |
| Requests / `SERVICE_REQUEST_REVIEW` | Begin review/request information | Agent | assigned item | Staff MFA | transition/view | Confidential | Customer Operations |
| Requests / `SERVICE_REQUEST_RESOLVE` | Resolve/reject under approved rules | Agent, Checker, Supervisor | assigned item/queue | Recent MFA where sensitive | decision + evidence refs | Confidential | Customer Operations + Compliance |
| Directory / `CUSTOMER_DIRECTORY_READ` | Purpose-bound masked search/results | Authorized Agent | org/product/region | Staff MFA | search and record view | Restricted | Privacy + Data Owner |
| Accounts / `ACCOUNT_OVERSIGHT_READ` | Masked read-only account/status/balance | Authorized Agent | customer case/product | Staff MFA | account view | Restricted | Banking Operations + Privacy |
| Audit / `AUDIT_EVENTS_READ` | Bounded investigation cases only | Auditor, Security Investigator | approved case/domain | Recent MFA | query/view/export denial | Highly restricted | Security + Compliance |
| Outbox / `OUTBOX_MONITOR_READ` | Operational metadata, no sensitive payload | Platform Operator | service/environment | Staff MFA | monitor view | Confidential | Platform Operations |
| Settings / `OPERATIONS_CONFIGURATION_READ` | Redacted readiness/config status | Supervisor, Security Reviewer | environment | Recent MFA | configuration view | Confidential | Security |

Exports, impersonation, unrestricted search, ledger mutation, event deletion, and manual replay are not included. Proposed roles grant nothing in the current application.
