# Phase I Architecture

Phase I is Mode A only: an isolated synthetic Angular demonstration. `OperationsDemoService` owns in-memory fixtures and has no `HttpClient`, banking API, customer repository, session, or ledger dependency. `operationsDemoGuard` permits the route only when `production=false` and `useMockApi=true`; production redirects to login.

Mode B is disabled. Both browser and bearer Spring Security chains deny `/api/v1/operations/**` before role evaluation. Proposed staff entitlements are documentation only. The customer shell contains no Operations navigation item.

No database migration, staff record, privileged user, financial endpoint, ledger path, or configuration mutation was introduced.
