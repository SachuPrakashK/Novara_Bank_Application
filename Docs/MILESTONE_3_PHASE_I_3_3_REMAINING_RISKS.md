# Milestone 3 Phase I.3.3 Remaining Risks

1. C: reached zero free bytes; approved npm-cache cleanup recovered about 8.40 GB, but capacity should continue to be monitored.
2. Docker/WSL recovered and passed three consecutive Engine checks, but the earlier VM/container I/O errors remain relevant operational history.
3. PostgreSQL containers are healthy; `api-api-1` and `api-keycloak-1` were restarting, and PostgreSQL/Flyway plus current Keycloak regression has not been rerun.
4. Four workforce secrets, controlled human identities, IAM authority, and authenticators are absent.
5. Genuine MFA and workforce session/revocation evidence is absent.
6. npm retains 24 transitive advisories; non-force remediation hit `ERESOLVE`.
7. Manual specialist accessibility acceptance is incomplete.
8. Gates A-G lack formal approval.

The approved npm-cache cleanup and WSL/Docker restart are complete. Docker VHD/images/volumes were not deleted and must not be treated as generic cleanup targets.
