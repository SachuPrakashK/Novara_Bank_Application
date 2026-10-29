# Staff Entitlement Lifecycle Results

| Scenario | Result |
| --- | --- |
| Approved synthetic grant | **VERIFIED WITH MOCKS** |
| Missing approval reference / invalid interval | **VERIFIED WITH MOCKS — denied** |
| Expired or future assignment | **VERIFIED WITH MOCKS — denied** |
| Revocation | **VERIFIED WITH MOCKS — next evaluation denied** |
| Scope reduction | **VERIFIED WITH MOCKS — old scope denied, new scope allowed** |
| Cross-organization/queue access | **VERIFIED WITH MOCKS — denied** |
| Concurrent stale update | **VERIFIED WITH MOCKS — version conflict** |
| Role removal | Represented by entitlement revocation; **VERIFIED WITH MOCKS** |
| Real staff suspension/provider revocation | **BLOCKED** |

Consistency guarantee is process-local and immediate because the POC has no cache. This is not a production persistence or distributed-revocation design.
