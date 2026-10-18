CREATE TYPE hold_status AS ENUM ('ACTIVE', 'RELEASED', 'CAPTURED', 'EXPIRED');

ALTER TABLE bank_accounts
  ADD COLUMN minimum_balance_minor bigint NOT NULL DEFAULT 0 CHECK (minimum_balance_minor >= 0),
  ADD COLUMN overdraft_limit_minor bigint NOT NULL DEFAULT 0 CHECK (overdraft_limit_minor >= 0);

CREATE TABLE account_holds (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  account_id bigint NOT NULL REFERENCES bank_accounts(id),
  amount_minor bigint NOT NULL CHECK (amount_minor > 0),
  currency char(3) NOT NULL CHECK (currency = 'INR'),
  status hold_status NOT NULL DEFAULT 'ACTIVE',
  reason varchar(80) NOT NULL,
  external_reference varchar(120) NOT NULL,
  expires_at timestamptz NOT NULL,
  released_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (account_id, external_reference),
  CHECK ((status = 'ACTIVE' AND released_at IS NULL) OR status <> 'ACTIVE')
);
CREATE INDEX idx_account_holds_active ON account_holds(account_id, expires_at) WHERE status = 'ACTIVE';

ALTER TABLE idempotency_records
  ADD COLUMN principal_subject varchar(200) NOT NULL DEFAULT 'legacy',
  ADD COLUMN operation_type varchar(60) NOT NULL DEFAULT 'LEGACY';
ALTER TABLE idempotency_records DROP CONSTRAINT idempotency_records_pkey;
ALTER TABLE idempotency_records ADD PRIMARY KEY (principal_subject, operation_type, idempotency_key);
CREATE INDEX idx_idempotency_expiry ON idempotency_records(expires_at);

ALTER TABLE transfers
  ADD COLUMN initiating_subject varchar(200) NOT NULL DEFAULT 'legacy',
  ADD COLUMN operation_type varchar(60) NOT NULL DEFAULT 'INTERNAL_TRANSFER';
ALTER TABLE transfers DROP CONSTRAINT transfers_idempotency_key_key;
ALTER TABLE transfers ADD CONSTRAINT uq_transfer_principal_operation_key
  UNIQUE (initiating_subject, operation_type, idempotency_key);

CREATE FUNCTION reject_financial_record_deletion() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  RAISE EXCEPTION 'financial and audit records cannot be deleted';
END $$;
CREATE TRIGGER trg_audit_immutable BEFORE UPDATE OR DELETE ON audit_events FOR EACH ROW EXECUTE FUNCTION reject_financial_record_deletion();
CREATE TRIGGER trg_outbox_no_delete BEFORE DELETE ON outbox_events FOR EACH ROW EXECUTE FUNCTION reject_financial_record_deletion();

