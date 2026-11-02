# Phase I.3.3 PostgreSQL Regression

Current full Maven execution was attempted twice. `BankingIntegrationTest` failed before PostgreSQL startup because Testcontainers could not obtain a valid Docker environment; Docker returned HTTP 500/timeouts. Therefore schema creation, Flyway V1-V11, and the 59 integration scenarios are **BLOCKED / NOT EXECUTED** in this phase.

The Docker-independent suite passed: 38 discovered, 33 passed, 5 real-provider tests skipped. This includes authorization POC 18/18 and production isolation 1/1. No database migration or test was weakened.

The historical 59/59 PostgreSQL and Flyway result is not counted as current evidence.
