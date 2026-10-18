# Milestone 3 Phase C remaining risks

- All Milestone 2.6 release blockers remain: real-browser IdP MFA/session/logout/passkey/multi-replica and deployment controls are not independently verified.
- Provider-backed beneficiary resolution has a secure service boundary but no approved production provider/operations adapter. Existing unresolved records cannot be paid.
- High-risk scheduled occurrences correctly stop in `REQUIRES_REVIEW`; a step-up continuation/resume workflow is not yet implemented.
- UPI, IMPS, NEFT, and RTGS are unavailable; no external success is simulated by the production adapter.
- Receipt delivery is an ownership-protected, no-store authoritative JSON representation. PDF generation is deferred because no approved PDF infrastructure exists.
- Development-adapter Chrome smoke E2E passes, but real-backend/real-IdP E2E, scheduler crash-injection/multi-instance load evidence, accessibility audit, penetration testing, and deployment observability remain required before release.
- Pause/resume and administrative recovery endpoints are not exposed in the customer UI.
