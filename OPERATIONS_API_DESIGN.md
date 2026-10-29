# Operations API Design

Status: **PROPOSED — NOT ACTIVE**. `/api/v1/operations/**` remains deny-all.

| Method/path | Proposed DTO / query | Permission and scope | Audit / limits |
| --- | --- | --- | --- |
| GET `/dashboard` | counts/ages by authorized queue; date window | dashboard read; org/queue | view; short cache, rate limit |
| GET `/service-requests` | cursor/page, size <= 50, category/status/queue/date, allowlisted sort | request read; queue | query; deterministic order |
| GET `/service-requests/{id}` | masked customer summary, request, history, version | request read; assignment/queue | record view; 404 for out-of-scope |
| GET `/customers` | bounded exact/prefix criteria; masked summary | directory read; purpose/org | search audit; strict rate limit |
| GET `/customers/{id}/accounts` | masked references/status/ledger-backed balances | oversight read; active case | customer/account view |
| GET `/audit-events` | bounded date <= approved window, action/outcome/resource, cursor | audit read; investigation scope | query is audited; no payload secrets |
| GET `/outbox/summary` | supported backlog metrics and telemetry status | monitor read; environment/service | view; no raw payload by default |
| GET `/configuration` | redacted readiness flags only | configuration read; environment | view; no secrets |

Responses use opaque IDs, ISO timestamps, explicit masking metadata, pagination links/cursors, and no internal IDs. Errors: 400 invalid filter, 401 unauthenticated, 403 denied, 404 absent/out-of-scope, 409 stale state where future writes exist, 429 rate-limited, 503 dependency unavailable. Sorting/filtering are allowlisted. No mutation endpoint is proposed in this phase.
