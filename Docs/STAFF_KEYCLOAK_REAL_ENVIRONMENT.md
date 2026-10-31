# Staff Keycloak Real Environment

> Phase I.3.1 update (2026-10-31): Docker Desktop is stopped and the selected engine pipe is absent. The corrected preflight now returns exit 1 instead of a false positive. See `WORKFORCE_KEYCLOAK_ENVIRONMENT_VERIFICATION.md` and `DOCKER_RECOVERY_RESULTS.md`.

Status: configuration **IMPLEMENTED**; startup **BLOCKED** because the local Docker API did not respond.

| Property | Value |
| --- | --- |
| Image | `quay.io/keycloak/keycloak:26.0.8` |
| Binding | `127.0.0.1:8082` |
| Realm | `novara-workforce-test` |
| Browser client | `novara-workforce-poc` |
| Cryptographic verifier | `novara-workforce-verifier` |
| Storage | Container tmpfs; removed on teardown |
| Realm mount | Workforce file only, read-only |
| Normal Compose impact | None; separate Compose file |

Run the preflight and lifecycle scripts with process-scoped policy bypass where Windows blocks local scripts:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\workforce-poc\Test-WorkforcePocPreflight.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\workforce-poc\Get-DockerEnvironmentDiagnostics.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\workforce-poc\Start-WorkforcePoc.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\workforce-poc\Stop-WorkforcePoc.ps1
```

All four required secrets must be injected into the process by the approved test-secret mechanism. Scripts never print them. Do not restart Docker if doing so would affect unrelated containers.
