# UI/UX Issues Fixed

1. Replaced the single shell-only error banner with an application-level toast host.
2. Added success, error, warning, and information toast variants.
3. Added toast stacking (maximum four), duplicate suppression, configurable duration, persistent errors, actions, and dismissal.
4. Added accessible live-region priorities without stealing focus.
5. Made the customer sidebar navigation independently scrollable with a thin themed scrollbar.
6. Kept sidebar branding and footer outside the scrolling region.
7. Corrected the collapse/expand control's inherited full-width styling.
8. Added mobile drawer background scroll locking, initial focus, Escape dismissal, and focus restoration.
9. Standardized pointer and disabled cursors without affecting text inputs or static cards.
10. Made the operations request row keyboard operable with Enter and Space.
11. Contained authenticated-shell horizontal overflow at 320px and tightened the smallest header layout.
12. Replaced an uncaught missing-payment observable error with loading and safe error states.
13. Limited themes to explicit Light and Dark, with Dark as the default.
14. Corrected self-service hero surfaces and shared semantic color usage in dark mode.

No banking validation, transaction, security, authorization, or persistence behavior was relaxed.
