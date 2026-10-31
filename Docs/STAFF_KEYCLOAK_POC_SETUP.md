# Staff Keycloak POC Setup

Status: realm template **IMPLEMENTED**; provider execution **BLOCKED** pending approved secrets and controlled identities.

## Prerequisites

- Windows, PowerShell, Docker Desktop, Java 21, Maven wrapper, VS Code.
- A disposable secret supplied through an approved local secret mechanism; never commit it or place it in shell history.
- IAM-authorized disposable staff identities and MFA devices.

## Isolated startup

1. Copy the secret from the approved vault into process environment variable `NOVARA_WORKFORCE_POC_CLIENT_SECRET` using the organization's secure injection procedure.
2. Start a separate Keycloak container on `127.0.0.1:8082`, mounting only `keycloak/novara-workforce-test-realm.json` read-only into `/opt/keycloak/data/import/`, and use `start-dev --import-realm` only on a disposable workstation/network.
3. Do not attach the Novara production or ordinary development database. The realm contains no users.
4. Through an IAM-controlled session, create disposable identities, enroll genuine TOTP or approved WebAuthn, and record only opaque test references.
5. Configure any future test application at ports 18080/14200; ordinary Novara ports and customer client are not reused.

## Test execution

Run `backend\mvnw.cmd -Dtest=WorkforceAuthorizationPocTest test` for mock POC tests. Provider tests require a separately reviewed test harness; none currently exists and no direct-password grant should be enabled.

## Cleanup

Stop and remove the isolated container/volume, revoke disposable identities and client secret, clear process environment, and confirm ports 8082/18080/14200 are closed. Retain only approved privacy-safe test evidence.

## Troubleshooting

- Import failure: confirm the secret is injected and JSON is mounted read-only.
- Missing audience: inspect client mapper configuration, never edit token claims.
- MFA claim absent: stop; correct the provider authentication flow rather than fabricating `amr`.
- Issuer mismatch: ensure tests use `/realms/novara-workforce-test`, not the customer realm.
