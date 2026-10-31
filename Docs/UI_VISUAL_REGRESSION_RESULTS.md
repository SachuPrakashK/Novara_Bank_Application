# UI Visual Regression Results

Date: 30 October 2026

## Automated verification

| Check | Result |
|---|---:|
| ESLint (`npm run lint`) | Passed |
| Angular unit tests (`npm test -- --watch=false`) | 13 files, 25 tests passed |
| Production build (`npm run build`) | Passed |
| Focused color suite | 6 tests passed |
| Full Playwright suite (`npx playwright test --workers=1`) | 27 tests passed |

## Responsive matrix

| Width | Theme/surface assertions | Keyboard focus | Horizontal overflow |
|---:|---|---|---|
| 390 px | Passed | Passed | None |
| 768 px | Passed | Passed | None |
| 1440 px | Passed | Passed | None |

Light and dark theme token resolution passed in Chrome. Computed-style crawls covered all 12 primary customer navigation destinations, login, and all 7 operations destinations. The full suite also verified authentication branding, identity verification, shell navigation, cards, payments, self-service, statements, and receipts after the color changes.

## Notes

The first focused test attempt used a hard browser navigation after entering the in-memory customer demo and correctly failed because that navigation reset the demo session. The test was corrected to use application router navigation, then passed on all target viewports. This was a test-navigation issue, not a product color regression.
