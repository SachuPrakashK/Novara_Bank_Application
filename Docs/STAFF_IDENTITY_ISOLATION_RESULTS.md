# Staff Identity Isolation Results

| Boundary | Result |
| --- | --- |
| Separate realm, browser client, verifier client | **IMPLEMENTED** |
| Workforce realm absent from normal Compose | **VERIFIED BY CONFIGURATION** |
| Realm has zero committed users | **VERIFIED** |
| Customer `CurrentIdentity` unchanged | **VERIFIED BY DIFF/REGRESSION** |
| Workforce POC absent from production main sources/resources | **VERIFIED — 1/1 automated test** |
| Mock customer issuer rejection and scoped mapping | **VERIFIED WITH MOCKS — included in 18/18 POC tests** |
| Actual customer/workforce provider cross-rejection | **BLOCKED** |

Email is not used as an identity key. No customer record or production database is accessed.
