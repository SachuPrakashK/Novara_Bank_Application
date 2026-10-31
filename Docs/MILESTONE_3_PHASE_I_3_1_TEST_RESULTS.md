# Milestone 3 Phase I.3.1 Test Results

Date: 2026-10-31

| Command/category | Actual result |
|---|---|
| Corrected workforce preflight | FAILED AS EXPECTED — Docker unavailable, exit 1 |
| Compose model validation | PASSED |
| Docker-independent backend selection | 33 passed, 2 real-provider skipped, 0 failed |
| Workforce authorization POC | 18/18 passed — mocks |
| Production isolation | 1/1 passed |
| Real Keycloak acceptance | 0 executed, 2 skipped — blocked |
| PostgreSQL/Testcontainers | 59/59 passed; Flyway V1–V11 applied |
| Complete backend suite | 94 discovered; 92 passed, 2 provider tests skipped |
| Angular lint | PASSED, zero warnings |
| Angular unit | 29/29 passed across 15 files |
| Angular production build | PASSED; 353.53 kB initial raw / 96.68 kB estimated transfer |
| Operations Playwright | 3/3 passed |
| Complete Playwright | 36/36 passed with one worker |

The attempted unquoted Maven `-Dtest` command failed at PowerShell parsing before Maven ran; it is not counted as a test execution. The corrected quoted command produced the results above.
