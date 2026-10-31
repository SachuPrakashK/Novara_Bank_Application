# Milestone 3 Phase I.2 Test Results

Date: 2026-10-27

## A. Existing banking regression

| Check | Result |
| --- | --- |
| Backend compilation and complete suite | **VERIFIED — 91/91 total** |
| Existing banking/security tests | **VERIFIED — 73/73** |
| PostgreSQL integration tests | **VERIFIED — 59/59** |
| Flyway | **VERIFIED — V1-V11; no V12** |
| Angular lint | **VERIFIED — zero warnings** |
| Angular unit tests | **VERIFIED — 25/25 across 13 files** |
| Angular production build | **VERIFIED — 339.61 kB raw / 92.36 kB estimated transfer** |
| Operations demo Playwright | **VERIFIED — 3/3** |
| Complete Playwright regression | **VERIFIED — 21/21 with one worker** |

The backend total comprises 73 existing banking/security tests and 18 isolated POC tests. Ledger, payments, cards, deposits, loans, statements, profile, security, notifications, Phase H/H.1, and operations deny-all behavior remain covered.

## B. Isolated authorization POC

`WorkforceAuthorizationPocTest`: **18/18 VERIFIED WITH MOCKS**.

Covered: valid scoped access; customer/wrong issuer; wrong audience/client; invalid signature evidence; expired token; missing/stale MFA; suspended staff; invalid/future/expired/revoked entitlement; immediate revocation; scope reduction; cross-organization/queue denial; stale concurrent update; required purpose/approval; opaque security audit; self-approval; revoked checker; cross-scope checker; expired/stale/duplicate approval.

These are deterministic unit tests of test-only policy code, not cryptographic or provider acceptance evidence.

## C. Real Keycloak provider acceptance

**NOT RUN / BLOCKED — 0 tests.** No controlled workforce identities, approved MFA device/automation, or vault-delivered POC client secret were available. The userless realm JSON was syntax-validated only. No fabricated claim or customer demo authentication is counted as real evidence.

## D. Execution observations

One Angular unit attempt lost a Vitest worker while lint ran concurrently; the standalone rerun passed 25/25. One full Playwright attempt hit Chromium `ERR_INSUFFICIENT_RESOURCES`; after stopping the orphaned Novara dev server, the complete serial rerun passed 21/21. Failed infrastructure attempts are not counted as acceptance evidence.
