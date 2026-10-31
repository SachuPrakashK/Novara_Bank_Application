# Staff Segregation of Duties Tests

Status: **VERIFIED WITH MOCKS** using synthetic approval cases only.

- Maker self-approval is denied.
- Checker without current entitlement is denied.
- Cross-organization approval is denied.
- Expired approval is denied.
- Stale approval version is denied.
- Duplicate approval is rejected deterministically.
- Independent entitled checker within scope can approve.

No service request, customer, account, journal, or outbox row is read or changed. The POC does not execute an approved action.
