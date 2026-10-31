# Operations Threat Model

Status: **PROPOSED mitigations unless marked IMPLEMENTED**

| Threat / scenario | Existing control | Missing control | Severity | Proposed mitigation / verification | Owner |
| --- | --- | --- | --- | --- | --- |
| Customer-to-staff escalation | Operations deny-all **IMPLEMENTED/VERIFIED** | Approved staff boundary | Critical | Separate workforce trust; negative provider/API tests | IAM + Security |
| Forged role claims | Global deny-all; issuer/audience/signature validation | Entitlement resolution | Critical | Server assignment lookup; claim tampering tests | Security |
| Cross-customer access / IDOR | Customer SQL ownership checks | Staff purpose/scope policy | Critical | Opaque IDs, scope joins, 404 semantics, adversarial tests | API + Privacy |
| Unauthorized audit search | No real explorer | Approved audit entitlement/case | High | Bounded queries, query audit, export denial | Security + Compliance |
| Sensitive leakage | Synthetic masks; no HTTP demo | Field policy/DLP | High | DTO allowlists, masking tests, log review | Privacy |
| Insecure provisioning | No staff provisioning | Authoritative lifecycle | Critical | JML workflow, expiry, reconciliation/certification | IAM |
| Session theft / MFA bypass | Secure session controls, recent-MFA service for customer actions | Staff assurance policy/provider evidence | Critical | Phishing-resistant MFA, binding/revocation tests | IAM + Security |
| Approval self-authorization | No engine/actions | Maker-checker persistence/enforcement | Critical | Distinct actor constraint and concurrency tests | Operations Security |
| Outbox replay abuse | Replay absent; delete prevented | Approved recovery model | Critical | Dual control, idempotency proof, immutable audit | Platform + Security |
| Synthetic mode in production | Production guard/config **IMPLEMENTED/VERIFIED** | Deployment policy verification | High | CI artifact/config test and release gate | Frontend + Release |
| Filter-chain misconfiguration | Both chains deny operations **IMPLEMENTED/VERIFIED** | Continuous configuration assurance | Critical | Browser/bearer negative regression and security review | Security |

Residual risk remains unacceptable for real-data operations until Gates A-G pass.
