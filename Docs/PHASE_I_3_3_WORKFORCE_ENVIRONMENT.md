# Phase I.3.3 Workforce Environment

Static inspection confirms Keycloak 26.0.8, realm `novara-workforce-test`, loopback `127.0.0.1:8082`, separate browser/verifier clients, read-only import, tmpfs storage, resource caps, and separate Compose project `novara-workforce-poc`.

Required process inputs remain:

- `WORKFORCE_KEYCLOAK_ADMIN` — disposable bootstrap administrator name.
- `WORKFORCE_KEYCLOAK_ADMIN_PASSWORD` — bootstrap credential.
- `NOVARA_WORKFORCE_POC_CLIENT_SECRET` — confidential browser-client secret.
- `NOVARA_WORKFORCE_VERIFIER_SECRET` — non-human cryptographic verifier secret.

All four were absent. Docker stability samples failed three of three. The provider was not started, no value was printed or committed, and no cleanup targeted unrelated resources. Local technical acceptance is distinct from IAM/production approval.
