# Docker Stability Verification

Verified on 2026-11-06 IST without restart, prune, reset, storage repair, or container lifecycle changes.

| Check | Current evidence | Result |
|---|---|---|
| Docker API before regression | Three consecutive valid `version` and `info` samples | PASSED |
| Docker API after regression | Three consecutive Engine 29.8.2 responses | PASSED |
| Assigned Docker memory | 3,918,626,816 bytes | OBSERVED |
| Context | `desktop-linux` active; named-pipe endpoint responsive | PASSED |
| WSL | `docker-desktop` running on WSL 2 | PASSED |
| Persistent volumes | 25 listed; Novara and unrelated named volumes retained | PASSED |
| Container restarts | All nine running containers reported restart count 0 | PASSED |
| Novara PostgreSQL | Running, healthy, restart count 0 | PASSED |
| Novara customer Keycloak | Running, restart count 0, discovery endpoint returned HTTP 200 | PASSED for runtime availability |
| Container filesystem | Approximately 942.7 GB available in the observed Docker filesystem view | PASSED |
| Testcontainers lifecycle | PostgreSQL 17 container started; Ryuk cleaned the ephemeral test resources | PASSED |

No matching PostgreSQL or Keycloak I/O/corruption/no-space errors appeared in their current two-hour logs. Docker's host log still contains a 64-byte historical `ENOSPC` record dated 2026-11-02; it is retained as historical evidence and did not recur in current container logs.

Docker storage remains shared with unrelated API, Keycloak, PostgreSQL, Redis, and hospital-management workloads. A restart or cleanup was neither needed nor safe to perform gratuitously.
