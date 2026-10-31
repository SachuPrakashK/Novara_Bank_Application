# Phase I.1 Decision Register

All entries are **PENDING APPROVAL**; recommendations are not approvals.

| ID | Decision / options | Recommendation | Security impact | Approver | Dependencies / acceptance evidence |
| --- | --- | --- | --- | --- | --- |
| IAM-01 | Separate staff realm vs shared provider | Separate workforce realm/tenant | Critical identity boundary | IAM + Security | Architecture review, provider config and separation tests |
| IAM-02 | Staff MFA and freshness | Phishing-resistant MFA; recent auth for privilege | Critical | Security + IAM | Assurance policy and real-provider evidence |
| AUTH-01 | Role model | Narrow agent/supervisor/auditor/operator roles; no unrestricted admin | Critical | Operations Security | Permission matrix, SoD tests |
| AUTH-02 | Permission assignment | Approved, expiring server-side entitlements | Critical | Role/Data owners | JML and certification evidence |
| AUTH-03 | Organizational scopes | Queue/org/product scopes | High | Customer Operations | Scope taxonomy and denial tests |
| DATA-01 | Customer-data access | Purpose-bound masked minimum | Critical | Privacy + Data Owner | Field inventory, DPIA/legal review, masking tests |
| OPS-01 | Request workflow | Versioned state machine | High | Customer Operations + Compliance | Reason/evidence/message catalogue |
| OPS-02 | Queue ownership | Named owner and restricted membership | High | Customer Operations | RACI, continuity exercise |
| OPS-03 | SLA/escalation | Policy-defined clocks and alerts | Medium | Customer Operations | Calendar/pause/threshold decisions |
| SOD-01 | Maker-checker | Independent checker for sensitive classes | Critical | Operations Security + Compliance | Classification and self-approval tests |
| AUD-01 | Audit retention/tamper evidence | Restricted append-only external evidence store | High | Security + Legal + Compliance | Retention approval and integrity verification |
| BOX-01 | Outbox recovery | Idempotent, dual-control replay; no delete | Critical | Platform + Security | Schema/runbook/chaos and duplicate tests |
| MON-01 | Monitoring/alerting | Named on-call with tested alerts | High | Platform Operations | SLOs, thresholds, response exercise |
| DEP-01 | Production controls | Controlled CI/CD, secret vault, security acceptance | Critical | Security + Release Authority | Gates A-F, deployment evidence, rollback test |
