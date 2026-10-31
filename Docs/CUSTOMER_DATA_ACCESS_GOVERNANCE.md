# Customer Data Access Governance

Status: **PROPOSED — PENDING PRIVACY, LEGAL, SECURITY AND DATA-OWNER APPROVAL**

Every search requires business purpose, authorized queue/case, organizational scope, bounded criteria, rate limits, and an audit of both query and record view.

| Classification | Examples | Display rule |
| --- | --- | --- |
| Authorized operational display | Customer display name, service-request status/category, product type, account status | Minimum needed for assigned case |
| Masked by default | Email, mobile, address fragments, account reference, beneficiary reference, balances | Reveal only approved fragments; ledger-backed balance is read-only |
| Elevated approval | Full contact/address, KYC decision evidence, investigation-linked history, exports | Separate permission, recent MFA, case reference, additional audit |
| Prohibited in operations UI | Passwords, OTP/recovery secrets, tokens, session secrets, IdP subject, internal DB IDs, full account/card identifiers, government-ID values/images, unapproved KYC material | Never returned |

Request history is visible only within purpose and scope. Bulk export is denied by default. Retention and deletion follow an approved legal schedule; audit/legal holds override only through documented authority. Suspected misuse triggers access suspension, evidence preservation, Security/Privacy response, impact assessment, and required notification workflow.
