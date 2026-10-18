CREATE TYPE payment_status AS ENUM ('PENDING_AUTHORIZATION','SCHEDULED','PROCESSING','COMPLETED','FAILED','CANCELLED','REQUIRES_REVIEW');
CREATE TYPE payment_destination_type AS ENUM ('OWN_ACCOUNT','BENEFICIARY');
CREATE TYPE payment_schedule_status AS ENUM ('ACTIVE','PAUSED','COMPLETED','CANCELLED','REQUIRES_REVIEW');
CREATE TYPE payment_recurrence AS ENUM ('ONCE','WEEKLY','MONTHLY');

ALTER TABLE beneficiaries ADD COLUMN resolved_account_id bigint REFERENCES bank_accounts(id);
CREATE INDEX idx_beneficiary_resolved_account ON beneficiaries(resolved_account_id) WHERE resolved_account_id IS NOT NULL;

CREATE TABLE payment_limit_policies (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint REFERENCES customers(id),
  account_id bigint REFERENCES bank_accounts(id),
  method varchar(20) NOT NULL DEFAULT 'INTERNAL' CHECK (method = 'INTERNAL'),
  per_payment_minor bigint NOT NULL CHECK (per_payment_minor > 0),
  daily_minor bigint NOT NULL CHECK (daily_minor > 0 AND daily_minor >= per_payment_minor),
  customer_selected_daily_minor bigint CHECK (customer_selected_daily_minor > 0 AND customer_selected_daily_minor <= daily_minor),
  effective_at timestamptz NOT NULL DEFAULT now(),
  created_at timestamptz NOT NULL DEFAULT now(),
  CHECK (customer_id IS NOT NULL OR account_id IS NOT NULL)
);
CREATE UNIQUE INDEX uq_payment_limit_customer_method ON payment_limit_policies(customer_id,method) WHERE customer_id IS NOT NULL AND account_id IS NULL;
CREATE UNIQUE INDEX uq_payment_limit_account_method ON payment_limit_policies(account_id,method) WHERE account_id IS NOT NULL;

CREATE TABLE payment_intents (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  source_account_id bigint NOT NULL REFERENCES bank_accounts(id),
  destination_account_id bigint NOT NULL REFERENCES bank_accounts(id),
  beneficiary_id bigint REFERENCES beneficiaries(id),
  destination_type payment_destination_type NOT NULL,
  amount_minor bigint NOT NULL CHECK (amount_minor > 0),
  fee_minor bigint NOT NULL DEFAULT 0 CHECK (fee_minor >= 0),
  currency char(3) NOT NULL CHECK (currency='INR'),
  method varchar(20) NOT NULL CHECK (method='INTERNAL'),
  description varchar(140),
  status payment_status NOT NULL DEFAULT 'PENDING_AUTHORIZATION',
  requires_step_up boolean NOT NULL DEFAULT false,
  transfer_id bigint UNIQUE REFERENCES transfers(id),
  expires_at timestamptz NOT NULL,
  confirmed_at timestamptz,
  completed_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK (source_account_id <> destination_account_id),
  CHECK ((destination_type='BENEFICIARY') = (beneficiary_id IS NOT NULL)),
  CHECK ((status='COMPLETED') = (completed_at IS NOT NULL))
);
CREATE INDEX idx_payment_intents_customer_created ON payment_intents(customer_id,created_at DESC,public_id);
CREATE INDEX idx_payment_intents_customer_status ON payment_intents(customer_id,status,created_at DESC);

CREATE TABLE payment_status_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  payment_intent_id bigint NOT NULL REFERENCES payment_intents(id),
  status payment_status NOT NULL,
  reason_code varchar(60),
  occurred_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(payment_intent_id,status,occurred_at)
);
CREATE INDEX idx_payment_status_events_payment ON payment_status_events(payment_intent_id,occurred_at);

CREATE TABLE payment_schedules (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  source_account_id bigint NOT NULL REFERENCES bank_accounts(id),
  destination_account_id bigint NOT NULL REFERENCES bank_accounts(id),
  beneficiary_id bigint REFERENCES beneficiaries(id),
  destination_type payment_destination_type NOT NULL,
  amount_minor bigint NOT NULL CHECK (amount_minor > 0),
  currency char(3) NOT NULL CHECK (currency='INR'),
  description varchar(140),
  recurrence payment_recurrence NOT NULL,
  timezone varchar(60) NOT NULL,
  next_execution_at timestamptz NOT NULL,
  end_at timestamptz,
  maximum_occurrences integer CHECK (maximum_occurrences IS NULL OR maximum_occurrences > 0),
  executed_occurrences integer NOT NULL DEFAULT 0 CHECK (executed_occurrences >= 0),
  status payment_schedule_status NOT NULL DEFAULT 'ACTIVE',
  lease_until timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK (source_account_id <> destination_account_id),
  CHECK ((destination_type='BENEFICIARY') = (beneficiary_id IS NOT NULL))
);
CREATE INDEX idx_payment_schedules_due ON payment_schedules(next_execution_at,id) WHERE status='ACTIVE';
CREATE INDEX idx_payment_schedules_customer ON payment_schedules(customer_id,created_at DESC);

CREATE TABLE payment_schedule_occurrences (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  schedule_id bigint NOT NULL REFERENCES payment_schedules(id),
  occurrence_number integer NOT NULL CHECK (occurrence_number > 0),
  scheduled_for timestamptz NOT NULL,
  payment_intent_id bigint UNIQUE REFERENCES payment_intents(id),
  status payment_status NOT NULL,
  failure_code varchar(60),
  created_at timestamptz NOT NULL DEFAULT now(),
  completed_at timestamptz,
  UNIQUE(schedule_id,occurrence_number),
  UNIQUE(schedule_id,scheduled_for)
);

CREATE FUNCTION reject_completed_payment_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.status='COMPLETED' AND NEW.status<>'COMPLETED' THEN RAISE EXCEPTION 'completed payments are immutable; use a controlled reversal'; END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER trg_completed_payment_immutable BEFORE UPDATE ON payment_intents FOR EACH ROW EXECUTE FUNCTION reject_completed_payment_mutation();
