# Milestone 3 — Phase G.1 Remaining Risks

- **BLOCKED — real identity acceptance:** Keycloak login/logout/refresh/expiry and genuine MFA assurance require controlled test credentials and an automatable provider ceremony.
- **NOT RUN — visual PDF acceptance:** multipage structure is programmatically valid, but rendered pages were not visually inspected for clipping.
- **BLOCKED — multilingual PDFs:** no approved embedded Unicode font exists. PDF generation rejects unsupported glyphs. Production needs a licensed, embedded font plus verified customer-language coverage.
- **Sandbox thresholds:** database-shared generation/download limits are production-compatible in topology, but final counts and window policy need security/product approval.
- **Scale:** valid periods remain capped at six months and page size at 100. Exports load the complete bounded interval in memory; production-volume load and heap tests remain necessary.
- **Legacy V9 generations:** records created before V10 have no immutable line cutoff and are deliberately blocked from regeneration rather than misrepresented.

Phase H may proceed only for sandbox development after stakeholders accept the explicit legacy block. Production progression remains blocked by real Keycloak/MFA acceptance, approved Unicode PDF font coverage, visual PDF acceptance, final rate-limit policy, and volume testing.
