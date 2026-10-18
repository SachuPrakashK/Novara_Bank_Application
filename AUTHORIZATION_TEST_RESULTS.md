# Authorization test results

| Status | Boundary | Actual result |
|---|---|---|
| PASS | Missing authentication | Protected account API returned 401. |
| PASS | Account BOLA/IDOR | Random/other account identifier returned 404 without disclosure. |
| PASS | Identity mapping | Exact issuer+subject required; unknown and RESTRICTED customers returned 403. |
| PASS | Financial authorization | Unauthorized, restricted-account, insufficient-funds, and held-funds cases posted no unauthorized value. |
| PASS | Operations least privilege | Missing operations authority returned 403; reconciliation requires scope or Operations/Administrator role. |
| PASS | High-value step-up | Missing/stale/single-factor assurance rejected; recent provider `auth_time` plus OTP/WebAuthn/MFA `amr` accepted by unit tests. |
| PASS | Open redirects | Non-allowlisted `returnTo=//evil.example/...` was not propagated and fell back to the fixed login route. |
| BLOCKED | Every workforce role matrix | Teller, Branch Manager, and Compliance have no implemented business endpoints beyond the tested operations boundary. |

Customer IDs supplied by Angular are never used to select the acting customer.
