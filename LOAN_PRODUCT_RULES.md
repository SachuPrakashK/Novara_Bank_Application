# Loan Product Rules

All products are explicitly fictional sandbox products denominated in INR.

| Code | Principal range (minor units) | Tenure | Fixed rate |
|---|---:|---:|---:|
| `SANDBOX_PERSONAL` | 500,000–50,000,000 | 6–60 months | 12.00% p.a. |
| `SANDBOX_EDUCATION` | 1,000,000–100,000,000 | 12–120 months | 0.00% p.a. |

Each quote snapshots the product version, rate, method, fee, dates, and calculated schedule. Current sandbox processing fees are zero. The server rejects out-of-range principal or tenure values. Product version `SANDBOX-2025-01` is illustrative and is not a public offer.
