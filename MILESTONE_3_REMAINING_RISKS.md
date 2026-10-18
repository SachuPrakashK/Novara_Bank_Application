# Milestone 3 remaining risks

## Release blockers inherited from Milestone 2.6

- Completed real-browser OIDC MFA login/callback and authenticated session restoration remain unverified.
- Browser logout/back-button behavior, global logout policy, passkey lifecycle, key rotation, multi-replica session revocation, and target deployment controls remain incomplete or unverified.
- Cross-browser E2E infrastructure and evidence remain absent.

These blockers prevent production financial activation even though Phase A/B automated tests pass.

## Phase B integration risks

- Beneficiary verification has a transactional internal boundary but no approved verification-provider or privileged operations adapter. Customers cannot self-activate a beneficiary.
- Recent MFA is validated for deactivation, but the product still needs an explicit step-up initiation/continuation journey rather than relying only on rejection and reauthentication.
- API rate limiting remains a target gateway responsibility and has not been validated in a deployment environment.
- The HMAC key requires secret-manager storage, access control, backup, and a dual-key rotation procedure before production use.
- Beneficiary payment eligibility, cooling enforcement, and recent-beneficiary ranking must be enforced by the Phase C backend payment workflow; the current UI cannot authorize a payment.
- UPI, IMPS, NEFT, and RTGS are not connected to banking rails. They remain unavailable integration boundaries.
- Independent accessibility, penetration, load, recovery, and browser E2E testing remain required.

## Phase C

Phase C-specific limitations and verification gaps are maintained in `MILESTONE_3_PHASE_C_REMAINING_RISKS.md`. External rails remain unavailable, unresolved beneficiaries remain unpayable, and high-risk scheduled execution cannot bypass unfinished step-up continuation.

## Scope not yet implemented

Milestone 3 Phases D–I—cards, deposits, loan servicing, statements, dashboard/notifications, and final security regression—have not started and are not claimed complete.

