# Phase I API Contracts

## Implemented

No new backend API. Every `/api/v1/operations/**` request is denied in both session and bearer security chains, including callers presenting proposed operations roles/scopes.

The synthetic Angular adapter exposes typed in-process fixtures for dashboard metrics, service requests, customers, accounts, audit events and outbox indicators. It performs no HTTP calls.

## Future, not implemented

Any dashboard, queue, directory, oversight, audit, monitoring or settings API requires approved staff identity mapping, entitlements, scope, audit policy and security review. All lists must be bounded and deterministically sorted. No financial mutation contract is proposed.
