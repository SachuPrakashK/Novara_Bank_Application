ALTER TABLE statement_generations ADD COLUMN accounting_cutoff_line_id bigint;

CREATE TABLE document_rate_limits (
  customer_id bigint NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  endpoint varchar(40) NOT NULL,
  window_started_at timestamptz NOT NULL,
  request_count integer NOT NULL CHECK (request_count > 0),
  PRIMARY KEY(customer_id, endpoint, window_started_at)
);
CREATE INDEX idx_document_rate_limit_cleanup ON document_rate_limits(window_started_at);
