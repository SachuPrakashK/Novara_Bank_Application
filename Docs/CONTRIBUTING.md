# Contributing

Use focused feature boundaries, standalone components, strict TypeScript, semantic HTML, and design tokens. Do not add dependencies when Angular or the platform already provides the capability.

Before submitting changes:

1. Run `npm.cmd run lint`.
2. Run `npm.cmd test -- --watch=false`.
3. Run `npm.cmd run build`.
4. Keyboard-test changed flows and check narrow/mobile layouts.
5. Confirm no secrets, credentials, tokens, full financial identifiers, or sensitive logs were introduced.

Financial workflows require tests for validation, duplicate-submit prevention, uncertain outcomes, and accessible status communication. Never describe a frontend-only control as authoritative banking enforcement.
