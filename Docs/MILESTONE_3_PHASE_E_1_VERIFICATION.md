# Milestone 3 Phase E.1 Verification

Verified on 2026-10-25 in Windows, Asia/Calcutta, using Java 21.0.12.1, Spring Boot 3.5.16, PostgreSQL 17.11 in Testcontainers, Docker Desktop 29.8.2, Angular 22.2, Node/npm from the repository environment, and headless Chrome through Playwright.

## Findings

- Docker was initially inaccessible inside the restricted process but was available through approved external execution.
- Flyway successfully validated and applied migrations V1–V7 to an empty PostgreSQL 17 database.
- A genuine RD schedule defect was found: `status` was ambiguous in a joined PostgreSQL query. It was qualified as `o.status` and the corrected integration suite passed.
- FD confirmation was verified for authoritative terms, ownership, balance checks, atomic funding, balanced ledger entries, replay idempotency, concurrent duplicate handling, rollback, detail retrieval, and isolation.
- RD opening was verified for durable ordered occurrences and stable keys. Concurrent workers produced one posting for a due occurrence; insufficient funds did not complete the occurrence or attach a journal.
- The deposit worker remains based on `FOR UPDATE ... SKIP LOCKED`. Its test startup is delayed to prevent the background scheduler competing with explicitly driven worker tests.
- Unsupported interest posting, capitalization, maturity payout, closure, and renewal remain unavailable. Angular presents an unavailable-policy notice and no action buttons for these operations.
- Card details now expose merchant search, status/date filters, sorting, server paging, clear filters, loading, empty, and error states.
- Poppins/authentication-brand exception, desktop sidebar collapse/expand, mobile drawer, and horizontal-overflow behavior passed existing Playwright checks.

No Critical or High financial-integrity defect remains confirmed in the implemented scope.
