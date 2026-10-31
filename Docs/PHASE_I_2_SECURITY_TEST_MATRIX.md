# Phase I.2 Security Test Matrix

| Threat/test | Result |
| --- | --- |
| Customer-to-staff escalation | **VERIFIED WITH MOCKS — denied** |
| Forged administrator/operations role or permission | **VERIFIED WITH MOCKS — roles not authoritative; operations deny-all** |
| Wrong issuer/audience/client | **VERIFIED WITH MOCKS — denied** |
| Invalid signature / expired token | **VERIFIED WITH MOCKS — denied** |
| Missing/stale MFA | **VERIFIED WITH MOCKS — denied** |
| Revoked/expired/invalid entitlement | **VERIFIED WITH MOCKS — denied** |
| Cross-organization / cross-queue / synthetic IDOR | **VERIFIED WITH MOCKS — exact scope required** |
| Self, stale, cross-scope, duplicate approval | **VERIFIED WITH MOCKS — denied** |
| Session reuse after real provider logout | **BLOCKED** |
| Production activation of POC | **VERIFIED BY CONSTRUCTION** — test source only; no endpoint/profile; realm not mounted |
| Real operations access | **VERIFIED WITH MOCKS — denied in browser and bearer chains** |
| Financial mutation from POC | **VERIFIED BY CONSTRUCTION** — no banking dependency or runtime component |
