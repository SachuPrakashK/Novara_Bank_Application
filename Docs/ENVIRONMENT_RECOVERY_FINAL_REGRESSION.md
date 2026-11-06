# Environment Recovery Final Regression

Current execution: 2026-11-06 IST.

| Area | Current result | Evidence |
|---|---|---|
| Disk measurement | PASSED | C: 6,010,384,384 bytes free; D: 72,687,611,904 bytes free |
| Preferred C: operating margin | BLOCKED | 8,989,615,616-byte gap to 15 GB; no cleanup authorized |
| Docker daemon/API | PASSED | Three valid samples before and three after regression; Engine 29.8.2 |
| Docker/WSL integrity | PASSED with historical risk retained | WSL 2 running, zero running-container restarts, no current Novara I/O errors |
| PostgreSQL/Flyway | PASSED | Testcontainers PostgreSQL 17; Flyway V1–V11; integration 59/59 |
| Backend complete suite | PASSED / SKIPPED | 97 total: 92 passed, 5 provider tests skipped, 0 failures/errors |
| Workforce authorization POC | PASSED | 18/18 |
| Production isolation | PASSED | 1/1; operations runtime remains deny-all |
| Workforce Keycloak crypto | SKIPPED / BLOCKED | 5/5 skipped; four secrets absent and port 8082 occupied |
| Human MFA | BLOCKED | No approved identity, factor, secret delivery, or IAM authority |
| Provider sessions/revocation | BLOCKED | No authorized human provider session |
| Angular lint | PASSED | Exit code 0 |
| Angular unit | PASSED | 29/29 across 15 files |
| Angular production build | PASSED | Production bundle generated |
| Playwright | PASSED | 37/37 in Chrome, including full route and responsive UI audits |
| D:-scoped build environment | PASSED | Child-process paths under ignored `tmp/tooling`; parent environment unchanged |

The initial non-elevated Angular attempt was environment-blocked by `spawn EPERM`; the authorized rerun passed. The first Maven command contained an incorrect repository-relative path and did not start tests; the corrected current execution produced the results above.
