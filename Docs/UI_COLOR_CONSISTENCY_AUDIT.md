# UI Color Consistency Audit

Date: 30 October 2026

## Scope

The audit covered every stylesheet and inline component style, the shared theme tokens, global browser-control behavior, all primary customer routes, authentication, every operations route, dialogs, status messages, and responsive layouts.

## Findings

1. The root document declared `color-scheme: light dark` even when the application was using its light theme. Browsers could therefore choose OS-dependent native colors for selects and other controls.
2. Inputs and selects did not share explicit semantic control tokens. Several feature styles used local `--novara-input-bg` fallbacks to literal white.
3. Cards, secondary buttons, modal panels, list states, and status messages in cards, deposits, loans, and statements contained literal light-theme colors. These bypassed the supported dark theme.
4. Keyboard focus covered buttons, anchors, inputs, and selects, but not textareas.
5. Deliberate white foregrounds on navy brand/hero panels are valid contrast treatments and were retained.
6. The automatic system-dark branch defined only part of the dark palette, allowing light status and elevated-surface values to leak into pages using the device theme.
7. `--novara-text-muted` and `--novara-gold-700` were consumed by multiple pages but were not defined centrally.

## Risk assessment

- High: native select and option colors could conflict with the active application theme.
- Medium: literal white modal/card surfaces could become bright islands in dark mode.
- Medium: literal status colors could lose contrast or visual meaning across themes.
- Low: textarea focus treatment was inconsistent with other controls.

## Acceptance criteria

- Surfaces and controls resolve through semantic CSS variables.
- Light and dark themes explicitly set their native `color-scheme`.
- Primary, secondary, disabled, status, and dialog states remain readable.
- Keyboard focus is visible for all interactive form controls.
- No horizontal page overflow occurs at 390, 768, or 1440 pixel widths.
- Existing customer and operations workflows continue to pass automated tests.
