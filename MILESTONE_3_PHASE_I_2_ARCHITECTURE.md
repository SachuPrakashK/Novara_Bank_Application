# Milestone 3 Phase I.2 Architecture

The POC is compiled exclusively from `backend/src/test`. It cannot be loaded into a packaged application. It uses a fixed workforce issuer, audience, and client identifier; immutable issuer/subject mapping; opaque internal staff IDs; server-side synthetic entitlements; business purpose; organization/resource scope; validity; revocation; MFA presence/freshness; and deny-by-default decisions.

The in-memory store deliberately has no cache, so revocation and scope reduction affect the next evaluation. Optimistic versions reject stale concurrent changes. Approval cases enforce maker/checker separation, checker entitlement, scope, expiry, version, and duplicate handling.

`keycloak/novara-workforce-test-realm.json` is a separate, userless realm template. It references an injected secret and is not mounted by the normal compose stack. It is preparation, not provider acceptance evidence.

Existing customer identity, Security filter chains, `/api/v1/operations/**`, Angular routes, databases, Flyway V1-V11, and all financial services are unchanged.
