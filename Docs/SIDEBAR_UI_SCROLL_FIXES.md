# Sidebar UI and Scroll Fixes

The customer sidebar is a fixed-height flex column with three regions:

1. A non-scrolling branding header.
2. A `min-height: 0`, vertically scrollable navigation area with contained overscroll and no horizontal scrollbar.
3. A non-scrolling security/sign-out footer.

The scrollbar uses `scrollbar-width`/`scrollbar-color` and WebKit styling while remaining visible and operable. Collapsed navigation keeps centered icons, titles, accessible names, and active highlighting.

The mobile drawer now locks body scrolling, receives focus after opening, closes on Escape, restores focus to the menu button, uses an overlay, and closes after navigation. Playwright verifies scrolling, fixed region geometry, collapsed/expanded geometry, focus behavior, scroll locking, and mobile overflow.
