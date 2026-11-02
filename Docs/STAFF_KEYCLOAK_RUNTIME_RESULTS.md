# Staff Keycloak Runtime Results

## Prior genuine provider evidence

The controlled Phase I.3.2 run on 2026-10-31 started Keycloak 26.0.8, imported `novara-workforce-test`, verified live discovery/JWKS, and passed five genuine non-human cryptographic acceptance tests. See `WORKFORCE_KEYCLOAK_RUNTIME_RESULTS.md` and `STAFF_REAL_OIDC_ACCEPTANCE_PHASE_I_3_2.md`.

## Current 2026-11-02 check

Initial bounded diagnostics reached Docker 29.8.2 with about 3.92 GB assigned, Java 21, Maven, Chrome, and free port 8082. All four required process-secret inputs were absent. The Docker API later returned HTTP 500 consistently during Testcontainers execution. Keycloak was therefore **NOT STARTED / NOT RERUN**; no secret was invented, printed, or persisted, and unrelated workloads were not changed.
