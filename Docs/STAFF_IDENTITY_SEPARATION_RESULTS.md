# Staff Identity Separation Results

| Control | Result |
| --- | --- |
| Immutable issuer/subject external key | **VERIFIED WITH MOCKS** |
| Separate opaque internal staff ID | **VERIFIED WITH MOCKS** |
| Customer issuer rejected | **VERIFIED WITH MOCKS** |
| Wrong audience/client rejected | **VERIFIED WITH MOCKS** |
| Invalid-signature evidence rejected | **VERIFIED WITH MOCKS** |
| Expired token rejected | **VERIFIED WITH MOCKS** |
| Suspended/unmapped staff rejected | Model implemented; provider lifecycle **BLOCKED** |
| Stable subject with real provider | **BLOCKED** |

The customer `CurrentIdentity` is unchanged. No workforce identity is inserted into `customers`, and no customer subject is registered in the POC resolver.
