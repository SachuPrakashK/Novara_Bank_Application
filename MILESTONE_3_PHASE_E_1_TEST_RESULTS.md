# Milestone 3 Phase E.1 Test Results

Results recorded 2026-10-25 (Asia/Calcutta).

| Check | Command | Result |
|---|---|---|
| Docker engine | `docker ps` | PASS — Desktop Linux engine reachable; existing PostgreSQL/Redis containers healthy |
| Angular lint | `npm.cmd run lint` | PASS |
| Angular unit tests | `npm.cmd test -- --watch=false` | PASS — 11 files, 20 tests |
| Angular production build | `npm.cmd run build` | PASS — lazy deposits bundle generated |
| Spring compile | `backend\\mvnw.cmd ... test` compile phase | PASS — Java 21, 64 main sources |
| Full backend suite | `backend\\mvnw.cmd -Ddebug=false -Dlogging.level.org.springframework.jdbc.core=INFO test` | PASS — 51 tests, 0 failures/errors |
| PostgreSQL integration | same Maven command | PASS — 42 integration tests |
| Flyway validation | Spring/Testcontainers startup | PASS — 7 migrations validated |
| Flyway migration | Spring/Testcontainers startup | PASS — V1–V7 applied to empty PostgreSQL 17.11 schema |
| Interest engine | full Maven suite | PASS — 5 deterministic financial tests |
| Typography/sidebar/mobile UI | `npm.cmd run e2e -- e2e/shell-consistency.spec.ts` | PASS — 3 tests |
| Card filter UI | `npm.cmd run e2e -- e2e/card-activity-filters.spec.ts --retries=0` | PASS — 1 test |

An earlier integration attempt failed one test because the RD schedule query used an ambiguous `status` column. This was a product defect, not an environment blocker; it was fixed and both the focused 42-test integration run and final 51-test backend run passed.

Real identity-provider login, MFA challenge completion, production card processing, and deployed-environment session behavior were not exercised. Security results cover Spring integration/unit tests and mock-mode browser flows only.
