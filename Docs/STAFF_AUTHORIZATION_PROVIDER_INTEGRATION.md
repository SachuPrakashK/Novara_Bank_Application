# Staff Authorization Provider Integration

Status: cryptographic adapter test **IMPLEMENTED BUT BLOCKED**; policy evaluator **VERIFIED WITH MOCKS**.

The conditional provider test validates real Keycloak token evidence before it could reach the Phase I.2 evaluator. The current service client deliberately does not map to a staff identity or entitlement. A future human-provider harness must translate only verified issuer/subject/client/assurance/time evidence; roles or permissions in tokens remain non-authoritative.

The evaluator continues to require active mapped staff, server-side entitlement, exact permission, organization/resource scope, business purpose, validity, recent MFA, and segregation-of-duties rules. It has no banking repository or runtime endpoint.
