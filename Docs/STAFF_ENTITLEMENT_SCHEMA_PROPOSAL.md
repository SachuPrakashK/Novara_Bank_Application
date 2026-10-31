# Staff Entitlement Schema Proposal

Status: **PROPOSED — NO MIGRATION CREATED**

V1-V11 cannot represent staff identity, assignments, organizational scope, queue membership, approval evidence, revocation, or access-review history. A future approved migration could introduce:

- `staff_identities(public_id, issuer, subject_hash_or_protected_subject, status, activated_at, deactivated_at, version)` with unique issuer/subject mapping.
- `staff_roles(code, description, risk_class)` and `staff_permissions(code, description)` plus immutable role-permission versions.
- `staff_assignments(staff_id, role_id, valid_from, valid_until, status, approved_by, approval_ref, revoked_at)`.
- `organizational_scopes` and `assignment_scopes(assignment_id, scope_type, scope_key)`.
- `operation_queues` and `queue_memberships(staff_id, queue_id, role, validity)`.
- `entitlement_decisions(requester, approver, decision, reason_code, evidence_ref, occurred_at)`.
- `access_reviews(campaign, reviewer, decision, evidence_ref, due_at, completed_at)`.

Constraints must prohibit self-approval, require bounded validity for privileged access, preserve history, use opaque public IDs, and avoid storing credentials. Provider subject storage and encryption/search strategy require Privacy and Security review.

Migration order: approve identity source and semantics; threat-model schema; prototype outside production; approve retention and keys; create reviewed Flyway migration; backfill none; deploy dark; reconcile; only then consider authorization consumption. V12 is intentionally absent.
