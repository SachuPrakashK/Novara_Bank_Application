CREATE TABLE statement_generations (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  account_id bigint NOT NULL REFERENCES bank_accounts(id),
  period_start date NOT NULL,
  period_end date NOT NULL,
  accounting_cutoff timestamptz NOT NULL,
  document_version integer NOT NULL DEFAULT 1 CHECK (document_version > 0),
  content_checksum char(64) NOT NULL,
  generation_status varchar(20) NOT NULL CHECK (generation_status IN ('COMPLETED','FAILED')),
  idempotency_key varchar(100) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  CHECK (period_end >= period_start)
);
CREATE UNIQUE INDEX uq_statement_generation_key ON statement_generations(customer_id,idempotency_key);
CREATE INDEX idx_statement_owner_created ON statement_generations(customer_id, created_at DESC);
CREATE INDEX idx_statement_account_period ON statement_generations(account_id, period_start, period_end);
CREATE INDEX idx_journals_posted_order ON journals(posted_at, id) WHERE status IN ('POSTED','REVERSED');
