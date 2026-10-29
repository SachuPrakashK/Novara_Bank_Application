# Milestone 3 Phase I Plan

## Baseline

- Existing operations surface: reconciliation POST only, method-role protected; no approved staff identities or assignments.
- Customer service requests and audit/outbox data are real and therefore unavailable to the demo.
- Angular had no operations shell, route, adapter or fixtures.
- Phase H.1 explicitly blocked staff processing and real IdP acceptance.

## Implemented sequence

1. Fail-close all real operations paths in both Spring Security chains.
2. Add a production-disabled Angular route guard.
3. Add a synthetic-only typed operations adapter.
4. Build dashboard, queue, directory, oversight, audit, monitoring and settings pages.
5. Keep all privileged actions disabled and explain dependencies.
6. Verify route isolation, masking, responsiveness and banking regressions.
