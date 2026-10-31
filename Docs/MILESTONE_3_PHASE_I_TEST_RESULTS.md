# Novara Bank — Milestone 3, Phase I Test Results

Date: 2026-10-26

## Result

Phase I is verified as an isolated, synthetic operations demonstration. Real operations APIs remain fail-closed because approved staff identities, assurance requirements, entitlements, and assignments are not available.

## Automated verification

| Check | Result |
| --- | --- |
| Backend Maven suite | PASS — 70/70 tests |
| Backend integration suite | PASS — 56/56 tests |
| Flyway validation/migration | PASS — V1 through V11 |
| Angular unit suite | PASS — 20/20 tests across 11 files |
| ESLint | PASS — zero warnings |
| Angular production build | PASS — initial bundle 339.61 kB raw / 92.36 kB estimated transfer |
| Focused Phase I Playwright suite | PASS — 3/3 tests |
| Complete Playwright regression | PASS — 21/21 tests using one worker |

## Security verification

- `/api/v1/operations/**` is denied in both bearer-token and browser-session security chains.
- An integration assertion confirms that even a caller presenting the proposed operations role and reconciliation scope receives HTTP 403.
- No production operations API was added or enabled.
- The operations demo uses only an in-memory synthetic data service and makes no backend request.
- The demo route is guarded by the development mock-adapter configuration and is not linked from the authenticated customer shell.

## Browser coverage

- Clearly labeled synthetic dashboard and isolated navigation.
- Service-request searching, detail display, pagination state, and disabled future actions.
- Masked customer directory and read-only account oversight.
- Safe audit presentation and unsupported outbox metrics shown as unavailable.
- Disabled replay controls and read-only readiness settings.
- Mobile layout at 390 px without document-level horizontal overflow.

## Execution note

The complete browser suite was deliberately executed with one worker. Earlier project verification established that parallel development servers can exhaust this workstation's available memory; serial execution produced a stable, fully passing result.
