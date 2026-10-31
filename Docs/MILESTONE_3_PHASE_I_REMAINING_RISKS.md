# Novara Bank — Milestone 3, Phase I Remaining Risks

Date: 2026-10-26

## Release position

Phase I is acceptable only as a development-only synthetic demonstration. It does not authorize production staff operations or access to real customer data.

## Blocking risks for real operations

1. **Staff identity is not approved.** No authoritative staff identity population, lifecycle, federation assurance, MFA policy, or emergency-access process has been accepted.
2. **Authorization is not approved.** Proposed roles and scopes are documentation only; assignments, segregation of duties, privileged-access reviews, and maker-checker rules are unresolved.
3. **Customer-data access governance is incomplete.** Purpose limitation, field-level masking, jurisdiction, retention, export controls, and access-review ownership require policy approval.
4. **Service-request operations are incomplete.** Assignment, SLA, escalation, approval, resolution reason codes, evidence retention, and notification contracts are not implemented.
5. **Audit operations are incomplete.** Production audit search, export authorization, tamper evidence, retention, and security-monitoring integration remain unavailable.
6. **Outbox operations are incomplete.** Failure state, attempt history, health telemetry, replay eligibility, idempotent replay, and delete policy are not implemented. Replay and deletion remain disabled.
7. **Operational ownership is missing.** On-call ownership, incident response, alert thresholds, runbooks, and support escalation have not been approved.

## Demonstration-specific risks

- Synthetic fixtures can drift from future production contracts and must never be treated as evidence of backend capability.
- The route is blocked by production configuration, but release packaging should continue to verify that no production navigation exposes the demo.
- Browser regression is run serially on the current constrained workstation; CI should provide a controlled resource envelope before increasing worker count.

## Required decision before Phase J

Do not start Phase J or enable any real operations endpoint until the Phase H.1 identity acceptance blockers and the Phase I authorization, data-access, workflow, audit, monitoring, and ownership decisions have named approvers and recorded acceptance criteria.
