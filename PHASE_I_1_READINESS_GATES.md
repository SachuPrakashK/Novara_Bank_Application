# Phase I.1 Readiness Gates

| Gate | Measurable acceptance criteria | Status |
| --- | --- | --- |
| A — Staff identity approved | IAM/Security approve architecture and JML/recovery/emergency process; controlled identities exist; issuer/subject separation and revocation tests pass | **BLOCKED** |
| B — Staff authorization approved | Role/permission/scope owners approve matrix; entitlement lifecycle and SoD are implemented; negative/expiry/self-approval tests pass | **BLOCKED** |
| C — Customer-data access approved | Field classification, purpose, masking, retention, export and incident rules approved; privacy/security tests pass | **BLOCKED** |
| D — Operational workflows approved | State machine, queues, owners, reason codes, evidence, SLA/escalation and continuity approved and tested | **BLOCKED** |
| E — Audit and monitoring approved | Event schema, retention, tamper evidence, access, alerts, on-call and runbooks implemented and exercised | **BLOCKED** |
| F — Provider acceptance completed | Real OIDC/MFA/session/lifecycle/CSRF acceptance plan passes with controlled identities and vault secrets | **BLOCKED** |
| G — Production security/deployment accepted | A-F complete; threat review, secret/config, CI/CD, observability, rollback/DR and release authority sign-off present | **BLOCKED** |

Phase I.1 documents define criteria but do not satisfy approval-dependent gates. Phase J recommendation: **NO-GO**.
