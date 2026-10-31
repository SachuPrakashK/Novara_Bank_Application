# Workforce Secret Readiness Review

Required inputs are `WORKFORCE_KEYCLOAK_ADMIN`, `WORKFORCE_KEYCLOAK_ADMIN_PASSWORD`, `NOVARA_WORKFORCE_POC_CLIENT_SECRET`, and `NOVARA_WORKFORCE_VERIFIER_SECRET`; startup rejects missing values.

The authorized local run generated opaque values with the platform cryptographic RNG, exposed them only to child processes, never printed them, and cleared them in `finally`. No `.env`, documentation, Angular bundle, or committed secret was created. No enterprise vault, IAM custodian, rotation, or production secret-delivery approval exists; formal readiness is **BLOCKED**.
