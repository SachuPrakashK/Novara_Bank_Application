# Milestone 3 Phase I.1 Test Results

Date: 2026-10-26

## Result

Phase I.1 safe controls are **VERIFIED**. Real operations remain **BLOCKED** and `/api/v1/operations/**` remains deny-all.

| Verification | Actual result |
| --- | --- |
| Backend test compilation | **VERIFIED** — passed |
| Complete backend suite | **VERIFIED** — 73/73 tests |
| PostgreSQL integration suite | **VERIFIED** — 59/59 tests |
| Flyway | **VERIFIED** — V1-V11 validated and migrated; no V12 |
| Angular lint | **VERIFIED** — zero warnings |
| Angular unit suite | **VERIFIED** — 25/25 tests across 13 files |
| Angular production build | **VERIFIED** — 339.61 kB raw / 92.36 kB estimated initial transfer |
| Focused operations Playwright | **VERIFIED** — 3/3 tests |
| Full Playwright regression | **VERIFIED** — 21/21 tests, one worker |

## Added security evidence

- Customer bearer access to operations is denied.
- Forged `ROLE_ADMINISTRATOR`, `ROLE_OPERATIONS`, and operations scopes do not bypass deny-all.
- A browser OIDC session with proposed operations authorities is denied.
- Browser customer sessions are denied.
- POST, PUT, PATCH, and DELETE shapes under operations remain denied; no financial mutation route was added.
- The production guard blocks the demo even if mock mode is accidentally enabled.
- Non-mock configurations block the demo.
- `OperationsDemoService` constructs without an HTTP provider and its records retain synthetic/masked markers.
- Existing ledger, payment, card, deposit, loan, statement, Phase H, and Phase H.1 regressions pass within the complete suites.

## Execution note

An initial full-browser attempt lost the Angular development server under workstation memory pressure while the backend was also resident. That incomplete run is not counted as acceptance evidence. After stopping only the local backend process, the same complete suite passed cleanly 21/21; the backend was restored afterward.

Real Keycloak staff/customer separation, MFA, revocation, refresh, and lifecycle acceptance tests are **BLOCKED** because controlled identities, approved MFA mechanisms, and vault-backed test secrets are unavailable.
