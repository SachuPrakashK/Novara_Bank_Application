# Card Management Design

The Cards workspace follows Novara's navy, muted-gold, neutral-surface design language and Poppins typography. It uses a responsive summary, selectable card list, masked SVG card preview, status badges, security controls, limit summaries, activity state, and card-service actions.

Desktop uses a two-column workspace; tablet makes card selection horizontally scrollable; mobile stacks content and controls without horizontal page overflow. Buttons use native semantics, switches expose `role="switch"` and `aria-checked`, status and error messages use live-region roles, and decorative SVG is hidden from assistive technology.

Security wording is deliberately precise: the screen labels the sandbox processor, displays masked references only, requires confirmation for freeze and sensitive service requests, and says requests are pending issuer review. It never claims a replacement was produced or dispatched.

Routes:

- `/cards` — card portfolio, controls, limits, and services.
- `/cards/:cardId` — ownership-scoped card profile and activity.
