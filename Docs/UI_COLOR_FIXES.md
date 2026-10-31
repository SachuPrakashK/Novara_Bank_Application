# UI Color Consistency Fixes

Date: 30 October 2026

## Shared theme layer

- Added semantic control tokens: `--novara-control-bg`, `--novara-control-text`, `--novara-control-placeholder`, and `--novara-control-border`.
- Added missing semantic aliases and shared overlay/switch tokens.
- Completed the full automatic system-dark palette so it matches explicit dark mode.
- Made dark the default theme and ensured each explicit theme advertises its matching native `color-scheme`.
- Added consistent native option colors and placeholder colors.
- Extended typography and visible focus treatment to textareas.
- Standardized the global disabled-button background, foreground, and cursor through action tokens.
- Updated the skip link to use semantic action colors.

## Feature remediation

- Cards: converted panels, selected navigation, controls, toggles, warning/success/error states, and action buttons to semantic tokens.
- Card details: converted surfaces, filters, and primary/secondary actions to shared tokens.
- Deposits: converted cards, tabs, form controls, actions, statuses, alerts, and modals to theme-aware tokens.
- Loans: converted cards, form controls, actions, statuses, alerts, schedules, and modals to theme-aware tokens.
- Statements: converted filters, panels, controls, badges, alerts, receipt details, and dialogs to semantic tokens.
- Authentication and shell: verified token-driven panels and standardized shared logo foregrounds.
- Payments, beneficiaries, self-service, dashboard, placeholders, and operations: audited shared surfaces, controls, actions, and dark-mode behavior.
- Shared card preview and status components: converted reusable foreground and status colors to semantic tokens.

Brand navy/gold styling, Poppins typography, and intentional light text on dark hero panels were preserved.

## Regression coverage

Added `e2e/ui-color-consistency.spec.ts` with assertions for:

- semantic dashboard and preference-page colors;
- native control and primary-action token resolution;
- light and dark theme behavior;
- keyboard focus visibility;
- responsive page overflow at 390, 768, and 1440 pixels.
- computed-style dark-theme crawls across all primary customer navigation routes, login, and all operations routes.
