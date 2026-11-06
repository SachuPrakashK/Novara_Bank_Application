# Environment Recovery Plan

Execution date: 2026-11-06 IST. This is post-Phase I.3.3 operational recovery and verification, not a new Milestone 3 phase.

## Safety boundaries

- Measure current storage, Docker, WSL, containers, volumes, logs, RAM, and tooling before acting.
- Preserve all containers, images, volumes, databases, credentials, and unrelated workloads.
- Do not delete caches, temporary files, build outputs, logs, Docker data, or virtual disks without separate explicit approval.
- Do not restart Docker/WSL while the daemon is healthy. Do not prune, reset, unregister, relocate, or repair persistent storage.
- Run existing backend, frontend, isolation, and browser regressions without changing banking or security behavior.
- Start the isolated workforce provider only if its four approved process secrets are present and port 8082 is available.

## Acceptance sequence

1. Reconcile Phase I.3.3 reports with current repository and environment state.
2. Inventory storage candidates and verify the D:-scoped build environment.
3. Require repeated valid Docker API responses and inspect container/storage integrity.
4. Run PostgreSQL Testcontainers, Flyway, backend, authorization, and production-isolation tests.
5. Run workforce preflight without inventing secrets or identities.
6. Run Angular lint, unit, production build, and Playwright.
7. Record PASSED, FAILED, SKIPPED, BLOCKED, or NOT RUN results and retain Gates A–G.

No destructive recovery action was necessary or authorized during this execution.
