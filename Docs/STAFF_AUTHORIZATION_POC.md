# Staff Authorization POC

Status: **IMPLEMENTED IN TEST SOURCE / VERIFIED WITH MOCKS**

An authorization decision requires all of:

- verified workforce issuer/signature, expected audience/client, unexpired token;
- mapped active staff identity using issuer/subject;
- recent MFA evidence;
- non-empty business purpose;
- active, unrevoked server-side entitlement;
- exact permission, organization, and synthetic resource-scope match;
- assignment validity at evaluation time.

Role claims and client-supplied staff IDs/permissions are not inputs. The POC emits privacy-safe allow/deny events with opaque actor references. It exposes no HTTP endpoint and cannot query banking repositories.
