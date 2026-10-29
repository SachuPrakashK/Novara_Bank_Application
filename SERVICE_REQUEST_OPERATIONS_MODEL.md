# Proposed Service Request Operations Model

This is a proposal, not implemented staff behavior.

| Concern                | Proposed control                                                             | Current status                                           |
| ---------------------- | ---------------------------------------------------------------------------- | -------------------------------------------------------- |
| Ownership              | Customer access by OIDC issuer/subject; staff access by assigned queue scope | Customer boundary implemented; staff boundary BLOCKED    |
| Staff roles            | Dedicated agent/supervisor roles with backend authorization                  | BLOCKED — roles, entitlements and assignments unapproved |
| Assignment             | Atomic queue claim with assignee identity, version and timestamp             | NOT IMPLEMENTED                                          |
| Review                 | `SUBMITTED → UNDER_REVIEW`; every change emits immutable history             | NOT IMPLEMENTED                                          |
| Additional information | Safe customer-visible prompt; never request secrets                          | NOT IMPLEMENTED                                          |
| Resolution/rejection   | Reason code and customer-safe response; supervisor rules where required      | NOT IMPLEMENTED                                          |
| Customer cancellation  | Policy-defined only while eligible; audited optimistic transition            | BLOCKED by policy                                        |
| Escalation             | SLA-driven queue/event with reason and ownership                             | BLOCKED by SLA design                                    |
| Communication          | In-app status event; external channels only after provider approval          | Submission notice implemented                            |

No customer identity receives staff privileges, no internal transition endpoint exists, and no employee or approval is simulated. Operations Security owns role design; Customer Operations owns queues and SLAs; Compliance and Privacy approve content and retention.
