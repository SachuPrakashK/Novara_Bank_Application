# Phase H API Contracts

All endpoints require authentication. Mutations under the browser BFF require `NOVARA-XSRF-TOKEN` / `X-XSRF-TOKEN`.

- `GET/PATCH /api/v1/profile`
- `GET/PUT /api/v1/profile/preferences`
- `GET /api/v1/service-requests?page=0&size=20&search=&sort=newest` returns page metadata; `POST /api/v1/service-requests` submits
- `GET /api/v1/service-requests/{id}`
- `GET /api/v1/service-requests/{id}/history`
- `GET /api/v1/security/summary`
- `GET /api/v1/security/sessions`
- `DELETE /api/v1/security/sessions/{sessionId}`
- `GET /api/v1/security/events`
- `GET /api/v1/notifications?page=0&size=25` returns `items`, page, size, total elements and total pages
- `GET /api/v1/notifications/unread-count`
- `POST /api/v1/notifications/{id}/read`
- `POST /api/v1/notifications/read-all`
- `GET/PUT /api/v1/notifications/preferences`

`POST /service-requests` also requires a 16–100 character `Idempotency-Key`. Ownership failures use non-enumerating 404 responses. Stale versions return 409.

