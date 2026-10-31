# Phase G Remaining Risks

- The ledger has posting timestamps but no separately governed effective/value date or backdating workflow.
- Six months is the deliberate complete-history limit; longer archives require an approved archival/snapshot design.
- PDF uses built-in Helvetica. Current customer-safe content is ASCII; production internationalization requires an approved embedded Unicode font and font licensing review.
- Application-level download rate limiting is not present in the existing platform and remains an infrastructure/release control.
- Browser acceptance with a production Keycloak ceremony and high-volume multi-page visual inspection remain release checks.
- Only existing authoritative internal transfers and deposit funding postings can appear. Unsupported settlement and loan financial events remain absent by design.

Automated verification does not establish production readiness. Phase H should not start until these operational controls and acceptance checks are reviewed.
