# Phase E Test Results

Results recorded 2026-10-25 (Asia/Calcutta).

- `npm.cmd run lint`: passed.
- `npm.cmd run build`: passed; production bundle generated, including lazy `deposits-page`.
- `mvnw.cmd -DskipTests compile` with repository JDK 21: passed.
- Backend unit selection: 9 tests passed (5 deposit financial examples plus 4 existing security/session tests).
- Full backend suite: 9 unit tests passed; the Testcontainers integration class could not start because no Docker environment was available. Consequently Flyway V1–V7 runtime validation is not claimed.
- Initial Angular test/build inside restricted sandbox: blocked by `spawn EPERM`; rerun outside sandbox for esbuild.

- Final `npm.cmd test -- --watch=false`: 10 files and 19 tests passed.
