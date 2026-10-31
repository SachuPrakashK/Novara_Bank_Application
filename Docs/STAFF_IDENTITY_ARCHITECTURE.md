# Staff Identity Architecture

Status: **PROPOSED — PENDING APPROVAL**

## Alternatives

| Option | Advantages | Risks / tradeoffs | Position |
| --- | --- | --- | --- |
| Separate staff realm/tenant | Strong customer/staff boundary, independent policies and lifecycle, clearer issuer trust | More federation and operational complexity | **Recommended**, pending IAM and Security approval |
| Shared provider, separate staff client and authoritative group source | Lower provider overhead | Claim confusion and customer-to-staff escalation risk require strict audience/issuer/client controls | Acceptable only after threat review |
| Shared customer client/realm | Simple | Customer identity could be mistaken for workforce identity | Rejected proposal |

## Proposed authoritative identity

Use immutable `(issuer, subject)` plus an internal opaque staff identifier. Email, username, employee number, and display name are mutable attributes and never identity keys. A trusted workforce source drives joiner/mover/leaver changes; application roles are provisioned from approved assignments, not self-asserted token claims.

## Lifecycle controls

- Joiner: verified employment, manager/sponsor, least-privilege request, approval, MFA enrollment, time-bounded activation.
- Mover: recertify scope; remove old access before or atomically with new access; terminate active sessions when risk changes.
- Leaver/suspension: disable provider account, revoke refresh/session state, expire application assignments, reconcile completion.
- Recovery: verified help-desk process; no MFA bypass; recovery events are privileged audits.
- Emergency access: separate vaulted identities, short duration, recent phishing-resistant MFA, incident reference, dual authorization, immediate alert and retrospective review.
- Reconciliation: compare workforce source, IdP, assignments, sessions, and application records; alert on orphaned or excess access.
- Certification: manager, resource owner, Security, and Privacy reviews at an approved cadence.

## Required claims and controls

Issuer, audience, subject, authentication time, assurance/MFA evidence, client identity, token validity, and session state must be verified. Roles alone are insufficient: the server also resolves active entitlement, organizational scope, business purpose, assignment/ownership, and segregation-of-duties constraints.

## Pending decisions

IAM owns realm/tenant, lifecycle, federation, and recovery. Security owns assurance, privileged access, emergency access, session termination, and certification policy. No option is approved by this document.
