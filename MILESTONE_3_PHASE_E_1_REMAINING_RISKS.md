# Milestone 3 Phase E.1 Remaining Risks

## Production blockers

- Real deposit rates, eligibility, disclosures, tax, nomination, holiday/calendar, closure, renewal, maturity, interest-posting, and chart-of-accounts policies are not approved.
- Interest accrual posting, capitalization, maturity payout, premature closure, and renewal therefore remain disabled.
- Card processor production commands and reconciliation require an approved provider and operational controls.
- Browser verification used the fictional mock customer. Real Keycloak login, recent-MFA challenge, logout propagation, session expiry, and deployment cookies require environment-level acceptance testing.
- Load, soak, backup/restore, observability alerting, accessibility audit, penetration testing, and formal compliance assessment remain outside this pass.

## Assessment

Phase E.1 automated verification is PASS for the implemented sandbox scope. PostgreSQL/Flyway is no longer blocked. Phase F should not begin unconditionally: production-policy and external-integration blockers remain, and explicit product-owner/finance/security approval is still required.
