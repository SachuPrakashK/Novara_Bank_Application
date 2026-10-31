# UI Route-by-Route Test Matrix

`PASS` means the route was actually visited by Playwright in the stated layout. Protected routes were reached with SPA navigation after entering the isolated demo, and final URLs were asserted.

| Route | Desktop | Mobile | Interactions | Theme | Result |
|---|---|---|---|---|---|
| `/login` | PASS | PASS | PASS | PASS | VERIFIED |
| `/auth/mfa` | PASS | PASS | PASS | PASS | VERIFIED |
| `/dashboard` | PASS | PASS | PASS | PASS | VERIFIED |
| `/accounts` | PASS | PASS | PASS | PASS | VERIFIED |
| `/accounts/acc-savings-01` | PASS | PASS | PASS | PASS | VERIFIED |
| `/transactions` | PASS | PASS | PASS | PASS | VERIFIED |
| `/transactions/txn-001` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/transfer` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/review` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/result` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/history` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/schedules` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/schedules/new` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/limits` | PASS | PASS | PASS | PASS | VERIFIED |
| `/payments/demo-missing` | PASS | PASS | PASS | PASS | VERIFIED ERROR STATE |
| `/beneficiaries` | PASS | PASS | PASS | PASS | VERIFIED |
| `/security` | PASS | PASS | PASS | PASS | VERIFIED |
| `/profile` | PASS | PASS | PASS | PASS | VERIFIED |
| `/account-services` | PASS | PASS | PASS | PASS | VERIFIED |
| `/notifications` | PASS | PASS | PASS | PASS | VERIFIED |
| `/preferences` | PASS | PASS | PASS | PASS | VERIFIED |
| `/cards` | PASS | PASS | PASS | PASS | VERIFIED |
| `/cards/card-demo-gold` | PASS | PASS | PASS | PASS | VERIFIED |
| `/deposits` | PASS | PASS | PASS | PASS | VERIFIED |
| `/loans` | PASS | PASS | PASS | PASS | VERIFIED |
| `/statements` | PASS | PASS | PASS | PASS | VERIFIED |
| `/investments` | PASS | PASS | PASS | PASS | VERIFIED PLACEHOLDER |
| `/support` | PASS | PASS | PASS | PASS | VERIFIED PLACEHOLDER |
| `/operations-demo/dashboard` | PASS | PASS | PASS | PASS | VERIFIED |
| `/operations-demo/requests` | PASS | PASS | PASS | PASS | VERIFIED |
| `/operations-demo/customers` | PASS | PASS | PASS | PASS | VERIFIED |
| `/operations-demo/accounts` | PASS | PASS | PASS | PASS | VERIFIED |
| `/operations-demo/audit` | PASS | PASS | PASS | PASS | VERIFIED |
| `/operations-demo/monitoring` | PASS | PASS | PASS | PASS | VERIFIED |
| `/operations-demo/settings` | PASS | PASS | PASS | PASS | VERIFIED |

Redirect-only and wildcard route records are routing mechanics, not separate visual views.
