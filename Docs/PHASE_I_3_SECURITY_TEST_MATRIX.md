# Phase I.3 Security Test Matrix

| Test | Evidence category | Status |
| --- | --- | --- |
| Issuer/audience/client/signature/expiry | Real Keycloak conditional tests | **BLOCKED (2 skipped)** |
| Tampered token/wrong audience | Real Keycloak conditional tests | **BLOCKED (2 skipped)** |
| Customer escalation, wrong issuer/client, missing/stale MFA | Pure policy | **VERIFIED WITH MOCKS** |
| Revoked/expired/cross-scope entitlement | Pure policy | **VERIFIED WITH MOCKS** |
| Unmapped/suspended staff | Pure policy | **VERIFIED WITH MOCKS** |
| Self/duplicate/stale approval | Pure policy | **VERIFIED WITH MOCKS** |
| No production POC class/resource | Production-isolation unit test | **VERIFIED 1/1** |
| Real human MFA/session/lifecycle | Real Keycloak/browser | **BLOCKED** |
| Operations browser and bearer deny-all | Existing integration regression | **VERIFIED previously; current full Docker suite blocked** |
| Financial mutation from POC | Dependency/config inspection | **VERIFIED BY CONSTRUCTION** |
