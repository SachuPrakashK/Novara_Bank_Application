CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE customer_status AS ENUM ('ACTIVE','RESTRICTED','CLOSED');
CREATE TYPE account_type AS ENUM ('SAVINGS','CURRENT','SETTLEMENT');
CREATE TYPE account_status AS ENUM ('ACTIVE','RESTRICTED','CLOSED');
CREATE TYPE ledger_classification AS ENUM ('ASSET','LIABILITY','EQUITY','INCOME','EXPENSE');
CREATE TYPE journal_status AS ENUM ('PENDING','POSTED','REVERSED');
CREATE TYPE entry_direction AS ENUM ('DEBIT','CREDIT');
CREATE TYPE transfer_status AS ENUM ('PROCESSING','COMPLETED','FAILED','REVERSED');

CREATE TABLE customers (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  identity_subject varchar(200) NOT NULL UNIQUE,
  display_name varchar(120) NOT NULL,
  status customer_status NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE ledger_accounts (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  code varchar(80) NOT NULL UNIQUE,
  name varchar(160) NOT NULL,
  classification ledger_classification NOT NULL,
  currency char(3) NOT NULL CHECK (currency = 'INR'),
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE bank_accounts (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  account_number_ciphertext bytea NOT NULL,
  account_number_hash char(64) NOT NULL UNIQUE,
  masked_number varchar(20) NOT NULL,
  nickname varchar(80) NOT NULL,
  type account_type NOT NULL,
  currency char(3) NOT NULL CHECK (currency = 'INR'),
  status account_status NOT NULL DEFAULT 'ACTIVE',
  debit_restricted boolean NOT NULL DEFAULT false,
  credit_restricted boolean NOT NULL DEFAULT false,
  ledger_account_id bigint NOT NULL UNIQUE REFERENCES ledger_accounts(id),
  version bigint NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE account_owners (
  account_id bigint NOT NULL REFERENCES bank_accounts(id),
  customer_id bigint NOT NULL REFERENCES customers(id),
  ownership_role varchar(30) NOT NULL CHECK (ownership_role IN ('PRIMARY','JOINT','AUTHORIZED')),
  can_debit boolean NOT NULL DEFAULT true,
  PRIMARY KEY(account_id, customer_id)
);
CREATE TABLE ledger_balances (
  ledger_account_id bigint PRIMARY KEY REFERENCES ledger_accounts(id),
  balance_minor bigint NOT NULL DEFAULT 0,
  version bigint NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE posting_batches (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  posting_reference varchar(100) NOT NULL UNIQUE,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE journals (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  posting_batch_id bigint NOT NULL REFERENCES posting_batches(id),
  journal_reference varchar(100) NOT NULL UNIQUE,
  currency char(3) NOT NULL CHECK (currency = 'INR'),
  status journal_status NOT NULL DEFAULT 'PENDING',
  description varchar(240) NOT NULL,
  reversal_of_journal_id bigint UNIQUE REFERENCES journals(id),
  posted_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  CHECK ((status = 'POSTED' AND posted_at IS NOT NULL) OR status <> 'POSTED')
);
CREATE TABLE journal_lines (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  journal_id bigint NOT NULL REFERENCES journals(id),
  line_number smallint NOT NULL CHECK (line_number > 0),
  ledger_account_id bigint NOT NULL REFERENCES ledger_accounts(id),
  direction entry_direction NOT NULL,
  amount_minor bigint NOT NULL CHECK (amount_minor > 0),
  currency char(3) NOT NULL CHECK (currency = 'INR'),
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(journal_id, line_number)
);
CREATE INDEX idx_journal_lines_ledger ON journal_lines(ledger_account_id, id);
CREATE TABLE transfers (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  source_account_id bigint NOT NULL REFERENCES bank_accounts(id),
  destination_account_id bigint NOT NULL REFERENCES bank_accounts(id),
  amount_minor bigint NOT NULL CHECK (amount_minor > 0),
  currency char(3) NOT NULL CHECK (currency = 'INR'),
  status transfer_status NOT NULL,
  journal_id bigint UNIQUE REFERENCES journals(id),
  idempotency_key varchar(100) NOT NULL,
  request_hash char(64) NOT NULL,
  failure_code varchar(60),
  created_at timestamptz NOT NULL DEFAULT now(), completed_at timestamptz,
  UNIQUE(idempotency_key)
);
CREATE INDEX idx_transfers_source_created ON transfers(source_account_id, created_at DESC);
CREATE INDEX idx_transfers_destination_created ON transfers(destination_account_id, created_at DESC);
CREATE TABLE idempotency_records (
  idempotency_key varchar(100) PRIMARY KEY,
  request_hash char(64) NOT NULL,
  resource_type varchar(40) NOT NULL,
  resource_public_id uuid,
  response_status integer,
  created_at timestamptz NOT NULL DEFAULT now(), expires_at timestamptz NOT NULL
);
CREATE TABLE audit_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  actor_subject varchar(200) NOT NULL,
  action varchar(100) NOT NULL,
  target_type varchar(60) NOT NULL,
  target_public_id uuid,
  outcome varchar(30) NOT NULL,
  correlation_id varchar(100),
  authorization_context jsonb NOT NULL DEFAULT '{}'::jsonb,
  occurred_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_actor_time ON audit_events(actor_subject, occurred_at DESC);
CREATE TABLE outbox_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  aggregate_type varchar(60) NOT NULL,
  aggregate_id uuid NOT NULL,
  event_type varchar(100) NOT NULL,
  payload jsonb NOT NULL,
  occurred_at timestamptz NOT NULL DEFAULT now(), published_at timestamptz
);
CREATE INDEX idx_outbox_unpublished ON outbox_events(id) WHERE published_at IS NULL;
CREATE TABLE reconciliation_runs (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  started_at timestamptz NOT NULL DEFAULT now(), completed_at timestamptz,
  status varchar(30) NOT NULL CHECK (status IN ('RUNNING','COMPLETED','FAILED')),
  exception_count integer NOT NULL DEFAULT 0 CHECK (exception_count >= 0)
);
CREATE TABLE reconciliation_exceptions (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  run_id bigint NOT NULL REFERENCES reconciliation_runs(id),
  exception_type varchar(80) NOT NULL,
  entity_type varchar(60) NOT NULL,
  entity_reference varchar(120) NOT NULL,
  details jsonb NOT NULL,
  detected_at timestamptz NOT NULL DEFAULT now()
);

CREATE FUNCTION reject_posted_journal_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.status IN ('POSTED','REVERSED') THEN RAISE EXCEPTION 'posted journals are immutable'; END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_journal_immutable BEFORE UPDATE OR DELETE ON journals FOR EACH ROW EXECUTE FUNCTION reject_posted_journal_mutation();

CREATE FUNCTION reject_posted_line_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE parent_status journal_status;
BEGIN
  SELECT status INTO parent_status FROM journals WHERE id = OLD.journal_id;
  IF parent_status IN ('POSTED','REVERSED') THEN RAISE EXCEPTION 'posted journal lines are immutable'; END IF;
  RETURN COALESCE(NEW, OLD);
END $$;
CREATE TRIGGER trg_journal_line_immutable BEFORE UPDATE OR DELETE ON journal_lines FOR EACH ROW EXECUTE FUNCTION reject_posted_line_mutation();

CREATE FUNCTION assert_posted_journal_balanced() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE debit_total numeric; credit_total numeric; currencies integer;
BEGIN
  IF NEW.status = 'POSTED' THEN
    SELECT COALESCE(sum(amount_minor) FILTER (WHERE direction='DEBIT'),0), COALESCE(sum(amount_minor) FILTER (WHERE direction='CREDIT'),0), count(DISTINCT currency)
      INTO debit_total, credit_total, currencies FROM journal_lines WHERE journal_id=NEW.id;
    IF debit_total = 0 OR debit_total <> credit_total OR currencies <> 1 THEN RAISE EXCEPTION 'journal % is not balanced', NEW.public_id; END IF;
  END IF;
  RETURN NEW;
END $$;
CREATE CONSTRAINT TRIGGER trg_journal_balanced AFTER INSERT OR UPDATE OF status ON journals DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION assert_posted_journal_balanced();
