# Novara Bank Global UI/UX Modernization

## Scope

This pass establishes a consistent, production-oriented presentation layer for every route currently exposed by the Novara customer application. It covers authentication, the authenticated shell, dashboard, accounts, transactions, payments, beneficiaries, schedules, security, support, and the honest placeholders for product areas that are not implemented yet.

The modernization deliberately does not invent banking capabilities or sample financial data. Existing application services, route guards, payment workflows, validation, and security boundaries remain authoritative.

## Design language

- **Brand:** restrained navy surfaces, warm gold accents, neutral content layers, and semantic success/warning/error colors.
- **Typography:** one application-wide hierarchy with compact labels, readable body text, tabular financial values, and responsive headings.
- **Spacing and shape:** centralized spacing, radius, elevation, icon-size, and motion tokens in `src/styles/_tokens.scss`.
- **Components:** shared shell, account cards, transaction rows, money formatting, status badges, controls, dialogs, and empty/error/loading states use the same visual grammar.
- **Motion:** short, functional transitions only. The global reduced-motion media query disables non-essential animation for users who request it.
- **Themes:** light and dark appearances use the same semantic variables rather than module-specific hard-coded palettes.

## Icon strategy

`IconComponent` is the controlled icon entry point. Its inline SVG paths inherit `currentColor`, remain crisp at every density, require no remote asset request, and expose decorative semantics consistently. Navigation, shell controls, account cards, transaction rows, authentication trust cues, and dashboard actions use this system instead of emoji or font glyphs.

New icons should be added to the `NovaraIcon` union and the component's SVG switch before use. Product code should not introduce independent icon libraries or raw emoji.

## Responsive application shell

- Desktop uses a persistent, grouped sidebar and a compact utility header.
- Tablet collapses secondary density while preserving primary navigation and readable financial content.
- Mobile provides an accessible menu trigger and bottom navigation, safe touch targets, wrapping controls, and horizontally contained data regions.
- Search routes to the existing transaction experience; theme, notification, session, and sign-out controls retain their existing behavior.

## Accessibility baseline

- Landmark navigation, visible focus treatment, accessible names, and screen-reader-only transaction direction context are provided.
- Interactive controls retain at least a 44px touch target where practical.
- Colors are expressed through semantic tokens and are not the sole carrier of account or transaction state.
- SVG icons are hidden from assistive technology when accompanied by text; icon-only controls receive explicit labels.
- Native controls and document structure remain keyboard operable.

## Module review

| Area | Outcome |
| --- | --- |
| Authentication and MFA | Branded entry experience and consistent trust/security presentation; authentication behavior unchanged. |
| Application shell | Rebuilt navigation, utility header, responsive mobile navigation, session dialogs, and toast presentation. |
| Dashboard | Consistent balance visibility control, account cards, transaction list, and route-backed quick actions. |
| Accounts | Shared account presentation, real status labels, and honest availability for unsupported summary actions. |
| Transactions | Shared iconography, money formatting, status treatment, and responsive list/detail patterns. |
| Payments and schedules | Premium workflow layout, validation, review, status, history, limits, schedule, and detail surfaces retained and aligned. |
| Beneficiaries | Existing management workflow retained within the common visual system. |
| Security, support, and profile | Existing screens inherit global shell, tokens, focus, dialog, and responsive behavior. |
| Cards, deposits, loans, investments, statements, and notifications | Existing honest placeholders remain placeholders and now inherit the same shell and icon language; no functionality was fabricated. |

## Asset decision

No raster photography was added. The current application is a privacy-sensitive, task-focused banking workspace where decorative imagery would add bundle weight and visual noise without improving comprehension. Locally rendered SVG iconography provides the useful visual guidance, ships with the application, and introduces no remote tracking or fragile asset dependency.

## Verification

Completed on 2026-10-18:

- ESLint: passed with zero warnings.
- Angular unit tests: 10 files, 19 tests passed.
- Angular production build: passed; initial bundle 315.57 kB raw / 87.33 kB estimated transfer.
- Playwright payment regressions: 2 tests passed in installed Chrome.
- Existing backend banking/payment integration baseline from the Milestone 3 work remains unchanged by this presentation-only pass.

## Extension rules

Future Cards, Deposits, Loans, Statements, and Administration work must reuse the semantic tokens, `IconComponent`, shell behavior, shared financial formatting, state patterns, and accessibility conventions documented here. A new module should add primitives to the shared layer only when they are reusable; it should not fork the brand palette or introduce a second visual vocabulary.
