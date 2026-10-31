# Global Design System Refactor

## Semantic foundations

- Added named XS–XXL spacing aliases while retaining the existing numbered aliases.
- Added a shared control-height token.
- Preserved the navy, gold, ivory, semantic status, surface, control, focus, and overlay tokens.
- Dark is the explicit default; Light remains the only alternative.

## Interaction foundations

- Links, enabled buttons, selects, summaries, and semantic button roles receive pointer cursors.
- Disabled controls receive `not-allowed`.
- Inputs and textareas retain their native text-edit cursor.
- Focus-visible styling remains token-driven.
- Reduced-motion behavior covers transitions and the new toast animation.

## Shared components

- `LogoComponent`: theme-appropriate horizontal assets and compact monogram.
- `ToastHostComponent`: responsive global notification presentation.
- `ErrorNotificationService`: safe, deduplicated notification state and lifecycle.
- `AppShellComponent`: responsive navigation, theme control, drawer focus, and scroll management.

No new component or icon library was introduced.
