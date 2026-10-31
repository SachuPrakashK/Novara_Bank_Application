# Staff Cross-Realm Isolation Results

| Control | Evidence | Result |
|---|---|---|
| Separate realm/issuer/loopback project | Runtime + static | PASS |
| Exact workforce issuer/audience/client | Real Keycloak | PASS |
| Wrong issuer/audience/signature | Real Keycloak | PASS |
| Customer issuer, forged role, unmapped/suspended staff | Mock policy | PASS |
| Matching-email non-linkage | Design/mock | PASS; no human runtime subject |
| Human cross-realm browser exchange | Real provider | BLOCKED |

Customer `CurrentIdentity`, realm/BFF, and data were unchanged; immutable issuer/subject remains the identity key.
