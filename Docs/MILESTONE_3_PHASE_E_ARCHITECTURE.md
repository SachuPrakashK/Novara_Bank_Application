# Phase E Architecture

The `deposit` package is a bounded module inside the existing Spring Boot modular monolith. Controllers resolve the authenticated OIDC identity; services enforce product, ownership, state, idempotency, and transaction rules; `LedgerPostingService` remains the sole posting path. PostgreSQL stores immutable contract snapshots and RD occurrences. The scheduled worker claims due rows with `FOR UPDATE ... SKIP LOCKED`.

Angular uses a lazy standalone `/deposits` page and the existing shell, tokens, Poppins typography, SVG icon component, XSRF client, account API, and error interceptor.
