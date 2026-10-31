# Novara Bank — Customer Banking Foundation

Premium Angular customer-banking foundation for Novara Bank. Milestones 1–3 cover the application shell, identity/session architecture, dashboard, accounts, transactions, beneficiaries, payment review and submission foundations, design system, HTTP security boundaries, and contract tests. All displayed data is fictional.

## Requirements and setup

- Node.js `^24.15.0` (verified with 24.18.0)
- npm 12+

```powershell
npm.cmd install
npm.cmd start
```

Open `http://localhost:4200`. Development configuration exposes **Explore the fictional customer demo**, or accepts structurally valid demo credentials followed by a six-digit MFA code. The demo entry point is omitted from production builds. These are UI demonstrations, not authentication security. On Windows systems that block PowerShell script shims, use `npm.cmd` rather than changing machine policy.

## Verify

```powershell
npm.cmd test -- --watch=false
npm.cmd run lint
npm.cmd run build
```

## Configuration

Development provides mock banking and identity adapters through injection tokens. Production environment replacement selects HTTP adapters and `/api/v1`; startup fails if production is ever configured with mocks enabled. The Milestone 2 backend is an OAuth 2.0 resource server and accepts bearer access tokens issued by the configured OIDC provider. OIDC browser login/token acquisition belongs in the deployment's provider SDK or BFF and no password/token-issuing endpoint is implemented here.

## Legacy frontend identity adapter contract

- `GET /api/v1/auth/session`
- `POST /api/v1/auth/sign-in`
- `POST /api/v1/auth/mfa/verify`
- `POST /api/v1/auth/session/continue`
- `POST /api/v1/auth/logout`

These routes remain an Angular adapter boundary and are not supplied by the Milestone 2 resource server. Banking calls include a unique `X-Request-ID`; the deployed OIDC integration must attach the bearer token. The backend remains responsible for token validation, resource authorization, rate limiting, and audit records.

## Milestone 2 banking API

- `GET /api/v1/me`
- `GET /api/v1/accounts` and account detail/balance/transactions routes
- `POST /api/v1/transfers/internal`
- `GET /api/v1/transfers/:transferId`
- `GET /api/v1/transfers/by-idempotency-key/:key`

Payment submission sends the generated idempotency key in the `Idempotency-Key` header. For Milestone 2 internal transfers, the review screen is a zero-fee client preview; account eligibility, funds, currency, posting, and final status remain authoritative on the backend. A network or server uncertainty is displayed as `UNKNOWN`, never as a confirmed failure.

```text
src/app/core       domain models, auth state, API contracts and adapters
src/app/layout     responsive authenticated shell
src/app/shared     reusable presentation primitives
src/app/features   lazy-loaded product areas
src/styles         semantic design tokens and themes
```

See [ARCHITECTURE.md](Docs/ARCHITECTURE.md), [SECURITY.md](Docs/SECURITY.md), and [CONTRIBUTING.md](Docs/CONTRIBUTING.md).

## Banking backend

The Java 21/Spring Boot backend lives in `backend/`. It provides OIDC-protected customer/account APIs, atomic double-entry internal transfers, idempotency, audit/outbox writes, and reconciliation against PostgreSQL. See [BACKEND_ARCHITECTURE.md](Docs/BACKEND_ARCHITECTURE.md), [LEDGER_DESIGN.md](Docs/LEDGER_DESIGN.md), [API_CONTRACTS.md](Docs/API_CONTRACTS.md), [DATABASE_SCHEMA.md](Docs/DATABASE_SCHEMA.md), and [RUNBOOK.md](Docs/RUNBOOK.md).
