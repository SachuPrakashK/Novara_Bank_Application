# Milestone 3 Phase I.3.2 Architecture

The workforce provider is a separate Compose project using `quay.io/keycloak/keycloak:26.0.8`, realm `novara-workforce-test`, loopback `127.0.0.1:8082`, read-only realm import, tmpfs, a 768 MiB cap, and one CPU. It has no banking database or customer-realm connection.

`novara-workforce-poc` remains an authorization-code/PKCE browser client with no direct or implicit grant. `novara-workforce-verifier` is a confidential service account used only for non-human cryptographic evidence; it has no staff mapping, entitlement, endpoint, or banking permission. `Invoke-WorkforceCryptoAcceptance.ps1` owns secret generation, execution, and `finally` cleanup.
