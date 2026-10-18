# Frontend review

The Angular application retains standalone lazy-loaded features, OnPush rendering, signals for local state, RxJS for API work, and dedicated API adapters. Production configuration selects `/api/v1` and rejects mock adapters.

Milestone 2.5 aligns account, dashboard, transaction, internal-transfer, and transfer-status DTOs with the backend. Production internal transfers select another authorized Novara account; unsupported external methods are not offered. The review page displays destination, amount, fee, and total debit, prevents repeat clicks, and rejects identical source/destination selection. Unknown outcomes are never shown as failure and can be queried using the original idempotency key.

The existing responsive shell, dark theme, keyboard-visible controls, loading skeletons, empty states, and semantic money formatting were preserved. A full assistive-technology/manual WCAG 2.2 AA audit was not performed and remains required before release.

The unresolved production boundary is OIDC browser integration. No token storage workaround was introduced; an approved provider SDK or BFF must supply tokens without persisting them insecurely.

