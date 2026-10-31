# Account Service Requests

Supported categories are account information, statement assistance, contact update assistance, card support, and general account support. Descriptions are limited to 500 characters and warn against entering secrets or government identifiers.

Customers can submit and view only their own requests. Submission requires CSRF and an idempotency key. Phase H creates only `SUBMITTED` events; no fake staff transition exists. `UNDER_REVIEW`, `ADDITIONAL_INFORMATION_REQUIRED`, `RESOLVED`, `REJECTED`, and `CANCELLED` are reserved for a future authorized operations workflow.

