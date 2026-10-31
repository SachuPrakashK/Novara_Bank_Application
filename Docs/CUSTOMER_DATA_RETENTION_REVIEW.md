# Customer Data Retention Review

No final legal period is asserted and no deletion job is authorized.

| Data                 | Proposed handling                                                             | Approval owner             |
| -------------------- | ----------------------------------------------------------------------------- | -------------------------- |
| Profile changes      | Current value plus audit; restricted access; policy-defined retention         | Privacy, Legal, Security   |
| Service requests     | Minimize text; redact discovered secrets; legal-hold-aware lifecycle          | Customer Operations, Legal |
| Request descriptions | 500-character limit, secret-pattern rejection, restricted logging/search      | Privacy, Security          |
| Notifications        | Minimal content; policy-controlled archive without deleting financial sources | Product, Legal             |
| Security events      | Append-only access controls; investigation window subject to policy           | Security, Legal            |
| Session records      | Expire through Spring Session; not a permanent activity record                | Identity, Security         |
| Outbox events        | Preserve until approved consumers and audit requirements are satisfied        | Platform, Finance Controls |

Production requires jurisdiction, legal basis, subject-access/erasure exceptions, legal hold, encryption, backup disposal and audit approval.
