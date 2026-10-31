# Workforce Keycloak Runtime Results

Execution: 2026-10-31 IST, isolated local technical test.

| Evidence | Result |
|---|---|
| Runtime | Keycloak 26.0.8; OpenJDK 21.0.6; Linux/WSL2 amd64 |
| Realm/endpoints | Realm imported; discovery and JWKS returned expected live data |
| Binding | Loopback `127.0.0.1:8082` only |
| Verifier | Genuine client-credentials token issued for cryptographic tests |
| Humans/browser | No identities created; browser flow not run |
| Cleanup | Container, network, tmpfs, and secrets removed; port closed |

Realm SHA-256: `8325A0B22382C3A146586A5ED0F5EDB92FA4053A3990600C56E8C25C6AFA7D14`
Compose SHA-256: `681D4B9B6539B317F2D20DA9109ED2E69ED358C08C00C1E97CA35C2B10E4506B`

No token, secret, credential, cookie, or OTP material was retained.
