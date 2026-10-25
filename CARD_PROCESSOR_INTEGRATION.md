# Card Processor Integration

`CardProcessor` is the provider-neutral outbound contract. Phase D supplies only `SandboxCardProcessor`, enabled when `novara.cards.processor-mode=sandbox` (environment variable `NOVARA_CARD_PROCESSOR_MODE`). Its references are prefixed `sandbox_` and the UI labels this mode.

Setting the mode to a non-sandbox value disables the simulated adapter. Startup then fails because no processor bean exists; this is intentional fail-closed isolation until a production adapter is implemented.

A future adapter must authenticate to the processor with secrets from an approved secret store, use request signing and transport security, map provider outcomes without leaking sensitive data, preserve provider request identifiers, define timeouts/retries, reconcile ambiguous outcomes, and emit operational telemetry. It must never log PAN, PIN, CVV, track data, access tokens, or provider credentials.

Provider acknowledgement is a prerequisite for local status/control mutation. An acknowledgement is not evidence of card issuance, manufacture, delivery, or transaction settlement.
