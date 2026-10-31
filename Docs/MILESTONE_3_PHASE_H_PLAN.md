# Milestone 3 Phase H Plan

## H0 gap analysis

| Area | Baseline | Phase H result |
|---|---|---|
| Identity and ownership | OIDC issuer/subject mapping, `CurrentIdentity`, CSRF and recent MFA existed | Reused for every API; no customer ID is accepted |
| Profile | `/me` returned only display name/status; profile UI was a placeholder | Authoritative read model and versioned preferred-name update |
| Contacts | No delivery or verification provider | Values remain read-only; verification cannot be client controlled |
| Account services | Account APIs and card-specific requests existed | General durable non-financial requests and shortcuts added |
| Security Center | Real JDBC sessions and audit events existed | Authoritative summary added; missing IdP claims are reported as unavailable |
| Notifications | Outbox existed for committed transfers; UI was a placeholder | Idempotent in-app projection, read state and preferences added |
| Language/preferences | English UI only | English-only preference, theme/date/reduced-motion persistence |

## Sequence completed

H1–H8 are implemented for sandbox scope. H9 automated regression passed; authenticated real-IdP browser acceptance is provider/credential blocked. H10 documentation is complete.

