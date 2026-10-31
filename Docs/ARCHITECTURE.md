# Frontend architecture

Novara is organized by product feature with a small `core` boundary and reusable, presentation-only `shared` components. Routes lazy-load standalone page components. Signals hold synchronous UI state; RxJS represents API streams. Components use `OnPush` change detection and depend on the `BANKING_API` contract rather than URLs or mock implementations.

Money is represented as integer minor units plus an ISO currency. This avoids treating binary floating-point values as domain amounts. Formatting is a presentation concern. The browser never calculates authoritative balances, fees, eligibility, limits, or ledger entries.

The authenticated shell adapts from a persistent desktop sidebar to an off-canvas tablet menu and a purpose-built mobile bottom navigation. Semantic tokens implement explicit light and dark themes, with dark as the default.

Customer Banking is the first deployable application. Business Banking, Staff/Teller, Operations, and Administration should become separate workspace applications when their milestones begin, sharing intentional libraries rather than feature internals.

```text
Page/component → BankingApi contract → MockBankingApi (development)
                                  ↘ HttpBankingApi (future backend)
```

Financial commands must carry backend-issued authorization context and idempotency keys. A timeout maps to processing/unknown until status is reconciled; it must not be presented as failure.

## Identity and HTTP boundary

`AuthService` owns presentation-facing authentication state but delegates authority to `AuthApi`. `HttpAuthApi` restores the server session before routing begins and uses dedicated sign-in, MFA, continuation, and logout endpoints. No access token is stored in Web Storage.

Functional interceptors apply only to the configured banking API. They attach credentials and an opaque request ID, normalize backend failures into safe error categories, retain correlation IDs for support, and expire local state on authenticated `401` responses. Raw backend messages are never surfaced.

`SessionService` provides the local idle warning experience. A backend expiry is an upper bound that browser activity cannot extend. Continuing a session requires a backend call and a new authoritative expiry.

## Beneficiaries and payments

`PaymentsApi` separates beneficiary, quote, submission, and status contracts from presentation. `PaymentStateService` keeps only the current ephemeral review flow, generates one idempotency key per prepared quote, and coalesces repeated submission calls behind the same Promise.

The review screen displays only backend-quoted amount, fee, total debit, method, and processing information. Angular performs input-shape validation and integer-minor-unit conversion for collection, but it does not determine fees, limits, available balance, beneficiary eligibility, authorization, posting, or final settlement.

Transport uncertainty becomes `UNKNOWN`. The UI warns against resubmission and retains the client request ID for reconciliation. Only an explicit backend result may produce `COMPLETED` or `FAILED`.
