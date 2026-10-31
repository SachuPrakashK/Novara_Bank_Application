# Novara Bank Branding Implementation

Updated: 31 October 2026

## Approved assets

The supplied horizontal logo and navy-and-gold monogram were prepared with the built-in image editing workflow. The horizontal artwork was background-extracted and tightly cropped without redesign; the monogram was tightly cropped while preserving its navy background. Favicon sizes were deterministically resized from the prepared monogram master.

Production assets:

- `/novara-logo.png` — 1200 × 409 horizontal transparent logo
- `/novara-logo-dark.png` — 1200 × 409 horizontal transparent logo with ivory `NOVARA` text for navy/dark surfaces
- `/novara-icon.png` — 512 × 512 compact monogram
- `/favicon.ico` — 32 × 32 ICO
- `/favicon-32x32.png` — 32 × 32 PNG
- `/favicon-16x16.png` — 16 × 16 PNG
- `/apple-touch-icon.png` — 180 × 180 PNG

## Application integration

- Replaced the CSS-drawn logo in the shared `LogoComponent` with approved image assets and semantic `auto`, `light`, and `dark` variants.
- Expanded dashboard sidebar and operations workspace use theme-appropriate horizontal logos.
- Collapsed sidebar uses the compact monogram.
- Login and MFA navy brand panels use the ivory-and-gold horizontal variant.
- The light mobile authentication panel uses the horizontal logo.
- The authenticated mobile header uses the compact monogram.
- Updated accessible alternative text to `Novara Bank`.
- Updated `index.html` favicon and Apple-touch metadata.
- Confirmed there is no web app manifest or registration route to update.

## Color consistency coverage

The existing application-wide semantic-token remediation remains in place. Computed-style Playwright crawls cover all primary customer navigation routes, login, and every operations route in dark mode, plus responsive checks at 390, 768, and 1440 pixels.

## Verification results

| Check | Result |
|---|---:|
| ESLint | Passed |
| Angular unit tests | 13 files, 25 tests passed |
| Production build | Passed |
| Branding asset tests | 3 tests passed |
| Focused authentication branding tests | 9 tests passed |
| Full Playwright suite | 30 scenarios completed; 29 direct passes and 1 retry pass |
| OTP retry confirmation | 1 test passed cleanly on immediate rerun |
| Production asset copy check | All 7 assets present |
| Missing branding asset requests | None |

No remaining branding or unintended-white-surface defects were found in the inspected routes.
