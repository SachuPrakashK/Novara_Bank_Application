# Phase I.3.3 Docker Recovery

## Diagnosis

Docker initially returned 29.8.2 server information, then Testcontainers received HTTP 500. Subsequent CLI `/version`, `/info`, and `/containers` requests timed out. Docker context and WSL commands still responded, Windows service state reported stopped, and port 8082 remained free.

Newest Docker logs show API proxy deadline failures dialing the Linux engine at `192.168.65.7:2376`. VM logs show loop-device read I/O errors, container-log write I/O errors, and PostgreSQL failing to read `global/pg_filenode.map`. C: has zero free bytes and holds the ~15.10 GB Docker VHD.

## Improvement

Diagnostics now classify `request returned`, HTTP 500, nonzero exits, and timeouts as failures. Workforce preflight requires three bounded valid API samples; current result is `{False, False, False}` rather than a false positive. No unlimited retry was added.

## Recovery status (2026-11-02 IST)

After the approved npm-cache cleanup, an approved Docker restart initially timed out. A scoped elevated `wsl --shutdown` followed by `docker desktop start --timeout 180` recovered the Docker WSL VM. `docker-desktop` is running on WSL 2, and Docker Engine 29.8.2 returned three consecutive valid server responses. No pruning, volume/image deletion, reset, unregister, or storage relocation was performed.

Post-recovery inspection found the PostgreSQL containers healthy. `api-api-1` and `api-keycloak-1` were restarting, so application/Keycloak readiness and the previously blocked full regression still require separate diagnosis and rerun; Docker/WSL recovery alone is not acceptance evidence.
