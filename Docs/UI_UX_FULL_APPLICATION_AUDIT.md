# Novara Bank UI/UX Full Application Audit

Audit date: 2026-10-31

## Scope and inventory

- 47 Angular component source files inspected.
- 36 reachable route views verified: 2 authentication, 27 customer, and 7 operations views.
- 17 SCSS/CSS files inspected, including semantic tokens and global browser-control rules.
- Standalone components and lazy route boundaries were preserved.
- Existing Angular/Vitest and Playwright infrastructure was used; no UI dependency was added.

## Findings

Fourteen actionable issues were identified and fixed: shell-local single-message notifications; missing toast tones, stacking, duplicate control, and timeout behavior; non-scrolling sidebar navigation; non-fixed sidebar regions; stretched collapse control; mobile drawer focus and page-scroll leakage; inconsistent interactive/disabled cursors; a mouse-only operations row; 320px shell overflow; an uncaught payment-detail error; device/system theme ambiguity; and remaining self-service light-surface leakage.

Shared fixes were preferred over page overrides. Banking APIs, authorization, identity, ledger, database, and operational security boundaries were not changed.

## Architecture reviewed

- Router: `src/app/app.routes.ts`
- Root application host: `src/app/app.ts`
- Customer shell: `src/app/layout/`
- Shared UI: `src/app/shared/ui/`
- Theme/design tokens: `src/styles/_tokens.scss`, `src/styles.scss`
- Safe API error normalization: `src/app/core/http/`
- Feature routes: `src/app/features/`
- Browser regression: `e2e/`

## Evidence

The production build, 29 unit tests, and 36 Playwright tests passed. The route crawl uses Angular SPA navigation and asserts the final URL, preventing false verification through auth redirects.
