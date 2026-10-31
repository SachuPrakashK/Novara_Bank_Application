# Phase G Architecture

`StatementService` resolves account ownership by identity issuer and subject, reads immutable `POSTED`/`REVERSED` journal lines, and establishes an accounting cutoff. Ordering is deterministic by `posted_at` then journal-line ID. Statement summaries use the complete interval independently of filters and pagination.

Generation persists owner, account, dates, cutoff, format version, and a SHA-256 content checksum—never duplicate transaction rows. Regeneration receives a new reference and cutoff. `StatementExportService` renders PDFs directly on the server with OpenPDF and produces UTF-8 CSV with formula neutralization. Receipts resolve only owned committed journal postings.

Angular uses the shared shell, tokens, icons, HTTP security interceptors, and a lazy-loaded Statements workspace. Production and demo adapters remain separated through dependency injection.
