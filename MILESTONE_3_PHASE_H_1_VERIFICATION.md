# Milestone 3 Phase H.1 Verification

| Area                                                    | Status                            | Evidence                                                               |
| ------------------------------------------------------- | --------------------------------- | ---------------------------------------------------------------------- |
| Profile ownership, CSRF, validation, optimistic locking | PASS                              | PostgreSQL/MockMvc regression suite                                    |
| Read-only regulated contacts and verification flags     | PASS                              | API shape and mutation regression                                      |
| Request ownership and non-enumeration                   | PASS                              | cross-customer integration test                                        |
| Request idempotency and concurrent retry                | PASS                              | simultaneous virtual-thread test; one request/history/notice           |
| Request paging/search/sort                              | PASS                              | bounded PostgreSQL query and integration test                          |
| Staff processing boundary                               | PASS                              | no staff transition endpoint or frontend control exists                |
| Notification commit/idempotency/read state/ownership    | PASS                              | outbox and notification integration regressions                        |
| Notification paging                                     | PASS                              | bounded page contract and UI controls                                  |
| Other-session termination                               | PASS at code/integration boundary | Spring Session repository deletes the owned server-side session        |
| Current-session termination                             | NOT RUN                           | UI intentionally does not advertise it; logout is the supported action |
| Real Keycloak/OIDC acceptance                           | BLOCKED                           | see `IDENTITY_PROVIDER_ACCEPTANCE.md`                                  |
| Mocked Phase H browser acceptance                       | PASS                              | 3/3 Chromium Playwright scenarios                                      |
| Complete browser regression                             | PASS                              | 18/18 Chromium tests with one worker                                   |
| Automated semantic/reflow checks                        | PASS for tested scenarios         | labels, roles, status announcements and 390px overflow check           |
| Manual screen reader and full WCAG 2.2 AA audit         | NOT RUN                           | specialist/manual tooling unavailable                                  |
| Realistic high-volume performance                       | NOT RUN                           | no approved production-shaped dataset or isolated load environment     |

The six-worker browser run exceeded the workstation's available resources and terminated the Angular dev server after 11 passes. The controlled serial rerun passed 18/18 in 49.7 seconds; this is test-environment evidence, not a production load result.

## Notification recovery

Transfer events remain in the durable outbox and projection is idempotent through `(customer_id, source_event_id)`. A temporary database failure rolls back the projection and a later notification list/unread request retries it. Operators can inspect unpublished `InternalTransferCompleted` events and compare them with notification source IDs. External monitoring, alerting, dead-letter routing and staffed ownership are BLOCKED pending an approved operations platform. Logs must contain event IDs/error codes only, never notification payloads or customer identifiers.
