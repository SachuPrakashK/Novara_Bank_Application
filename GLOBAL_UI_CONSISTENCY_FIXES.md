# Novara Bank Global UI Consistency Fixes

## Scope and files

This pass updates the shared Angular typography, semantic theme tokens, logo/icon primitives, application shell, authentication theme, dashboard surfaces, and payment surfaces. Banking routes, authentication protocols, financial calculations, API contracts, and session behavior are unchanged.

Primary files changed include `src/styles.scss`, `src/styles/_tokens.scss`, the shared logo and icon components, the application shell, authentication styles, dashboard and payment styles, package manifests, and the responsive Playwright coverage in `e2e/shell-consistency.spec.ts`.

## Typography

Poppins 400, 500, 600, and 700 are self-hosted through `@fontsource/poppins`. The files ship in the application bundle, use the package's `font-display: swap` declarations, and make no third-party font request. Poppins is now the default UI and display family, including financial amounts, controls, navigation, tables, dialogs, and authentication RHS content.

The login and identity-verification LHS panels explicitly set `--novara-font-display` and `font-family` to the existing Novara serif brand family. This scoped exception prevents the global UI font from replacing the editorial branding treatment.

No Keycloak theme templates are present in this repository. Production authentication redirects to the configured identity provider, so its hosted pages cannot be styled or rendered from this Angular source tree.

## Sidebar behavior

- Desktop navigation now has a functional compact rail controlled by the shared shell.
- The control uses directionally correct `panel-left-close` and `panel-left-open` SVGs and updates its label and `aria-expanded` state.
- Compact navigation retains centered icons, active state, destination labels through accessible names and native tooltips, and automatically reallocates space to the workspace.
- The signal lives on the persistent shell, so its state survives feature-route navigation without adding persistent tracking.
- At tablet/mobile widths, compact-rail behavior is disabled in favor of the existing drawer. Escape closes the drawer, while the backdrop and close control remain available.
- Width, margin, and transform changes use the shared motion token and inherit the global reduced-motion override.

## Theme and color audit

Semantic tokens now cover primary and secondary actions, disabled content, navigation hover/selection, information treatment, branded dark panels, and elevated hover surfaces. The audit corrected the highest-impact mismatches:

- authentication primary buttons no longer combine the pale primary surface with white text;
- dark-theme primary actions use muted gold with dark readable text;
- sidebar hover and selected states use navigation-specific tokens;
- session-dialog and shared secondary actions use action tokens;
- dashboard and featured payment panels use a stable navy brand-panel token in both themes;
- payment primary/secondary controls use action tokens instead of assuming `--novara-primary` is always dark;
- header icon hover states use an elevated semantic surface.

White and neutral card surfaces remain where they provide necessary reading contrast. Brand illustration colors and translucent overlays remain intentionally fixed because they are artwork rather than semantic application states. Success, warning, danger, and information retain distinct token families.

## Accessibility and responsive verification

The sidebar toggle exposes its current action, compact links remain named, keyboard focus uses the shared focus ring, Escape dismisses the mobile drawer, and reduced-motion preferences remain respected. Automated coverage checks typography scoping, desktop collapse/navigation/expand behavior, mobile drawer dismissal, and horizontal overflow.

## Verification results

- ESLint completed with zero warnings.
- Angular unit tests passed: 10 files, 19 tests.
- Angular production build passed: 321.38 kB raw / 88.16 kB estimated initial transfer.
- Playwright passed: 12 tests covering authentication layouts and OTP, payments, typography scoping, desktop sidebar behavior, and the mobile drawer.
- Browser regression tests ran with two workers to avoid local development-server startup contention observed at six-way concurrency.

## Remaining issues

- Keycloak-hosted theme rendering is **BLOCKED** because no Keycloak theme assets or templates are mounted by this repository; only realm configuration is present.
- Automated browser contrast computation is not configured. Semantic foreground/background pairings were reviewed in both token sets, while full WCAG contrast automation remains a future test-infrastructure improvement.
