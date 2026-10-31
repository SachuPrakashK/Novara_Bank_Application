# Staff Test Identity Provisioning

Status: **BLOCKED — NOT EXECUTED**

No human workforce identity was created because no approved secret-delivery mechanism or controlled MFA device/automation was available. The committed realm contains zero users.

When IAM authorizes execution, provision opaque personas such as `agent-<random>`, `supervisor-<random>`, `auditor-<random>`, `suspended-<random>`, and `unassigned-<random>` through an authenticated administrative session. Generate credentials with a cryptographic RNG, deliver them only through the approved vault, require password change and genuine TOTP/WebAuthn enrollment, and record only opaque references. Never reuse customer subjects, real names, emails, or production credentials.

After testing: disable/delete identities, revoke sessions/client secrets, remove the container/volume, clear environment variables, and retain only sanitized evidence.
