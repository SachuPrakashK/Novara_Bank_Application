# Phase I.3.1 Production Isolation

- Workforce Compose remains separate and now has the explicit project name `novara-workforce-poc`.
- The realm contains zero users and is not mounted by normal Compose.
- All four secrets remain unresolved process inputs; none is committed or bundled.
- Workforce policy and provider acceptance code remains under backend test sources.
- Production-isolation JUnit test passed 1/1.
- Both Spring Security chains still deny `/api/v1/operations/**`.
- Customer `CurrentIdentity`, customer realm/BFF, and customer login were not changed.
- No staff endpoint, entitlement persistence, production profile, or Flyway migration was added.
- Angular operations remains a synthetic, read-only demonstration; operations Playwright passed 3/3.
- Frontend production build completed without workforce credentials.
- Ledger, payments, beneficiaries, cards, deposits, loans, statements, and financial posting behavior were not modified.
