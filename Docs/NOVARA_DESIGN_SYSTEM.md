# Novara Bank product design language

## Phase E application

The deposit workspace uses the existing Poppins font, navy/gold semantic palette, SVG icon component, surface/border/focus tokens, shell/sidebar, responsive breakpoints, reduced-motion behavior, accessible labels, alert regions, and modal semantics. Estimated returns are labelled separately from contributed/realized values, and all production-unsupported products are visibly identified as sandbox terms.

This standard applies to Payments and every future Cards, Deposits, Loans, Statements, and Administration module.

## Visual foundation

- Deep navy communicates trust and anchors primary actions; gold is a restrained accent for focus, selection, and premium detail.
- White and neutral surfaces carry content. Gradients are exceptional, never decorative defaults.
- Use the existing display/body type families, tabular numerals for money, shared radii, borders, focus rings, and shadow tokens.
- Use the reusable `novara-icon` SVG wrapper. Do not use emoji or mixed icon families. Decorative graphics are hidden from assistive technology; meaningful icons receive labels.

## Page grammar

Every module uses the same hierarchy: eyebrow, descriptive page title, concise support copy, one primary action, responsive summary cards, then task content. Tables collapse into readable mobile cards. Destructive or high-risk actions never compete visually with the primary path.

Every remote-data surface must define loading, empty, error, success, disabled, and retry behavior. Focus remains visible, controls have accessible names, color is never the only status signal, and reduced-motion preferences are honored.

## Reusable primitives

- SVG icon wrapper, page header, action/summary card, status badge, filter bar, confirmation panel, empty/error state, skeleton list, transaction row, timeline, and receipt/detail card.
- Primary buttons are navy; secondary buttons use neutral surfaces; gold indicates emphasis without reducing contrast.
- Financial confirmation always shows source, masked destination, amount, fees, total debit, method, authorization requirement, and authoritative status.

## Engineering guardrails

- UI state is never an authorization boundary and never fabricates financial success.
- Production adapters call supported backend APIs; mock adapters remain development-only.
- Use integer minor units or exact backend decimals. Never calculate authoritative money with JavaScript floating point.
- Keep feature routes lazy-loaded, assets local and optimized, and dependencies minimal.
