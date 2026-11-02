# Milestone 3 Phase I.3.3 Architecture

Phase I.3.3 adds no banking runtime capability. `scripts/Use-ProjectLocalBuildEnvironment.ps1` redirects npm cache, process temp, and Java temp to ignored `tmp/tooling` paths on D: for the current PowerShell process and children only. It does not alter user or machine settings.

Workforce Docker remains the isolated `novara-workforce-poc` Compose project: Keycloak 26.0.8, localhost port 8082, read-only realm import, tmpfs, separate clients, no customer data, and no production privilege. Preflight now requires three valid bounded Docker API samples so a transient response cannot hide subsequent daemon failure.

No staff endpoint, session chain, entitlement repository, migration, or financial mutation was added.
