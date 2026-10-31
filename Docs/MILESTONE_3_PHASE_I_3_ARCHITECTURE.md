# Milestone 3 Phase I.3 Architecture

The workforce provider is isolated in `docker-compose.workforce-poc.yml`: pinned Keycloak 26.0.8, localhost port 8082, separate `novara-workforce-test` realm, separate clients, read-only realm import, and tmpfs provider storage. It has no banking database, customer realm mount, production network exposure, or committed secret.

`novara-workforce-verifier` is a non-human service client used only to prove real discovery/JWKS signature, issuer, audience, client, expiration, tamper, and wrong-audience behavior. It is not a staff identity, has no banking entitlement, and cannot satisfy human MFA acceptance.

Real-provider JUnit tests are disabled unless `NOVARA_WORKFORCE_REAL_PROVIDER_TESTS=true` and an injected verifier secret is present. Production packages contain neither test classes nor the realm resource. No runtime endpoint or workforce security chain was added.

Customer `CurrentIdentity`, BFF, normal Compose, `/api/v1/operations/**` deny-all, Flyway V1-V11, and all financial services are unchanged.
