# Milestone 3 Phase H.1 Remaining Risks

| Status  | Missing dependency                            | Owner                                    | Required action                                                | Sandbox impact            | Production impact                    |
| ------- | --------------------------------------------- | ---------------------------------------- | -------------------------------------------------------------- | ------------------------- | ------------------------------------ |
| BLOCKED | Real Keycloak identities and MFA automation   | IAM, QA Security                         | Provision controlled identities and vault-backed automation    | Mock acceptance only      | Identity controls unaccepted         |
| BLOCKED | Staff roles, assignments, queues and SLAs     | Operations Security, Customer Operations | Approve backend role/entitlement and case model                | Requests stay SUBMITTED   | No operational fulfilment            |
| BLOCKED | Email/SMS/push/WhatsApp contracts             | Product, Procurement, Privacy            | Select providers and approve consent/templates/retries         | In-app remains functional | No external delivery                 |
| BLOCKED | Monitoring, alerting and dead-letter platform | Platform Operations, SRE                 | Define metrics, alert routes, recovery runbook and ownership   | Manual outbox inspection  | Reliability/SLA unaccepted           |
| BLOCKED | Legal retention schedule                      | Legal, Privacy, Security                 | Approve jurisdictional matrix and disposal/legal-hold controls | No deletion jobs          | Retention noncompliance risk         |
| NOT RUN | Manual screen reader/full WCAG audit          | Accessibility QA                         | Test supported browser/AT matrix and contrast/zoom             | Automated semantics only  | WCAG conformance unproven            |
| NOT RUN | Production-volume load/heap testing           | Performance Engineering                  | Supply representative anonymized data and isolated environment | Bounded queries verified  | Capacity unproven                    |
| BLOCKED | Global IdP logout/password/MFA management     | IAM                                      | Approve provider integrations/URLs                             | Local sessions only       | Account-security journeys incomplete |

Recommendation: Phase I may proceed only as isolated sandbox planning/development if it does not depend on these controls. Production progression is **NO-GO** until the responsible owners close the blockers.
