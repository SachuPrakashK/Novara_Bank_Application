# Phase I.3.2 Banking Regression

Executed across 2026-10-31 and 2026-11-01 IST.

| Suite | Result |
|---|---|
| Maven | 97 discovered; 92 passed; 5 provider tests skipped outside runner; 0 failed |
| Real Keycloak targeted | 5/5 passed |
| PostgreSQL/Flyway | 59/59; V1-V11 validated/applied |
| Authorization POC / isolation | 18/18 mock; 1/1 isolation |
| Angular | lint PASS; 29/29 unit; production build PASS |
| Playwright | operations 3/3; complete 36/36 |

Complete regressions cover banking/customer modules. No banking API or financial service changed.
