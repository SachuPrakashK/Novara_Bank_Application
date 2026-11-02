# Staff Real Authentication Results

| Area | Evidence | Current status |
|---|---|---|
| Discovery/JWKS/signature/issuer/audience/client/subject | Prior genuine Keycloak 26.0.8 service token | VERIFIED WITH REAL KEYCLOAK (2026-10-31) |
| Tamper, invalid signature, wrong issuer/audience, expiry | Prior genuine Keycloak tests 5/5 | VERIFIED WITH REAL KEYCLOAK (2026-10-31) |
| Current provider rerun | Four secrets absent; Docker API later HTTP 500 | NOT RUN |
| Human authorization-code/PKCE login | No approved disposable identity/factor | BLOCKED |
| Active/suspended/unmapped/unassigned humans | No approved provisioning authority | BLOCKED |

The verifier service account is non-human, unmapped, and has no banking entitlement. Mock authorization results are not counted as real authentication.
