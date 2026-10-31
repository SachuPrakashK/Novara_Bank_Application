# Service Request Workflow Design

Status: **PROPOSED**. Current real requests remain `SUBMITTED`; no staff transition API exists.

| From | To | Proposed actor/control | Evidence, message and events |
| --- | --- | --- | --- |
| SUBMITTED | UNDER_REVIEW | Assigned authorized agent; atomic claim/version check | assignment, purpose, `REQUEST_REVIEW_STARTED`; safe “under review” message |
| UNDER_REVIEW | ADDITIONAL_INFORMATION_REQUIRED | Assigned agent; approved reason | question template, due date; never request secrets; audit + notification |
| ADDITIONAL_INFORMATION_REQUIRED | UNDER_REVIEW | Customer response received or agent resumes | response reference, version check, audit |
| UNDER_REVIEW | RESOLVED | Agent or maker/checker according to request class | resolution reason, evidence refs, customer-safe outcome, audit + notification |
| UNDER_REVIEW | REJECTED | Authorized decision; checker where sensitive | rejection reason, evidence, safe explanation, audit + notification |
| SUBMITTED / ADDITIONAL_INFORMATION_REQUIRED | CANCELLED | Customer or authorized policy path | eligibility check, reason, audit + notification |

All writes require optimistic concurrency and return conflict for stale versions. Assignment/reassignment is atomic and scope-bound. SLA clocks, pause conditions, escalation thresholds, reason-code catalogues, evidence retention, and customer messages are **PENDING** Customer Operations, Legal, Privacy, and Compliance decisions.
