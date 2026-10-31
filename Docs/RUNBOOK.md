# Local and operational runbook

## Requirements

- Docker Desktop
- Node.js 24 and npm 12 for Angular
- Optional local Java 21 and Maven 3.9; Docker commands work without them

## Local database

```powershell
$env:NOVARA_DB_PASSWORD = 'local-only-password'
docker compose up -d postgres
```

Never reuse this password outside local development. Flyway runs when the backend starts.

## Backend build and tests

```powershell
docker run --rm -v "${PWD}/backend:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-21 mvn -B -DskipTests package
```

With Java 21 installed, run `mvnw.cmd verify` from `backend/`. The checked-in Maven wrapper supplies Maven; integration tests use Testcontainers and require Docker. Running Testcontainers from inside a Maven container also requires mounting the Docker socket and Docker Desktop host-network configuration, so the wrapper is the simpler Windows test path.

## Identity provider

Register a development API audience `novara-api` and customer client with an established OIDC provider. Set:

```powershell
$env:NOVARA_OIDC_ISSUER = 'https://idp.example.test/realms/novara'
$env:NOVARA_OIDC_AUDIENCE = 'novara-api'
```

The issuer must expose standard discovery/JWKS metadata. Never commit client secrets or tokens. Customer access tokens need a stable `sub`; operations reconciliation requires `operations.reconcile` scope.

## Production controls

Terminate TLS at an approved ingress, enforce secure headers and explicit CORS origins, store secrets in a managed secret store, encrypt disks/backups, use least-privilege database roles, scan images/dependencies, ship structured logs to restricted storage, alert on authorization failures/reconciliation exceptions, and regularly test restore and disaster-recovery procedures.

If a transfer response is lost, query by idempotency key. Do not submit with a new key. If reconciliation reports an exception, preserve evidence, stop unsafe posting paths if necessary, and remediate through controlled reversal—not direct ledger mutation.
