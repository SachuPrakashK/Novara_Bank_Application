# Phase I.3.2 Security Test Matrix

| Scenario | Evidence | Result |
|---|---|---|
| Discovery/JWKS/signature/issuer/audience/client/expiry/subject | Real Keycloak | PASS |
| Tamper, invalid signature, wrong issuer/audience, expiry rejection | Real Keycloak | PASS |
| Future not-before | Real Keycloak | BLOCKED |
| Escalation, forged claims, suspended/unmapped staff | Mock policy | PASS |
| Revoked/expired entitlement, scope/IDOR, maker-checker negatives | Mock policy | PASS |
| Missing/stale MFA | Mock/static | Real acceptance BLOCKED |
| Human session/revocation/reuse | Real provider | BLOCKED |
| Operations endpoints deny-all | Integration/static | PASS |

Evidence categories are intentionally not merged into one security pass count.
