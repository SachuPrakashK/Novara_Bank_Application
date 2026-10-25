CREATE TABLE deposit_products (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  code varchar(40) NOT NULL UNIQUE,
  name varchar(120) NOT NULL,
  deposit_type varchar(12) NOT NULL CHECK (deposit_type IN ('FIXED','RECURRING')),
  currency char(3) NOT NULL CHECK (currency = 'INR'),
  minimum_amount_minor bigint NOT NULL CHECK (minimum_amount_minor > 0),
  maximum_amount_minor bigint NOT NULL CHECK (maximum_amount_minor >= minimum_amount_minor),
  minimum_tenure_months integer NOT NULL CHECK (minimum_tenure_months > 0),
  maximum_tenure_months integer NOT NULL CHECK (maximum_tenure_months >= minimum_tenure_months),
  installment_frequency varchar(12) CHECK (installment_frequency IN ('MONTHLY')),
  interest_method varchar(20) NOT NULL CHECK (interest_method IN ('SIMPLE','COMPOUND_MONTHLY')),
  day_count_convention varchar(20) NOT NULL DEFAULT 'ACTUAL_365' CHECK (day_count_convention IN ('ACTUAL_365')),
  compounding_frequency varchar(20) NOT NULL CHECK (compounding_frequency IN ('NONE','MONTHLY')),
  payout_frequency varchar(20) NOT NULL DEFAULT 'AT_MATURITY' CHECK (payout_frequency IN ('AT_MATURITY')),
  premature_closure_allowed boolean NOT NULL DEFAULT false,
  renewal_allowed boolean NOT NULL DEFAULT false,
  grace_period_days integer NOT NULL DEFAULT 0 CHECK (grace_period_days >= 0),
  missed_installment_policy varchar(30) NOT NULL DEFAULT 'MARK_MISSED' CHECK (missed_installment_policy IN ('MARK_MISSED','RETRY_DURING_GRACE')),
  status varchar(12) NOT NULL CHECK (status IN ('ACTIVE','INACTIVE')),
  effective_from date NOT NULL,
  effective_to date,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK (effective_to IS NULL OR effective_to >= effective_from),
  CHECK ((deposit_type='RECURRING' AND installment_frequency='MONTHLY') OR (deposit_type='FIXED' AND installment_frequency IS NULL))
);

-- Phase D stabilization: stable ownership-scoped card activity paging/filtering.
CREATE INDEX idx_card_transactions_customer_card_status_date ON card_transactions(customer_id,card_id,status,occurred_at DESC,id DESC);
CREATE INDEX idx_card_transactions_merchant_search ON card_transactions(customer_id,card_id,lower(merchant_description) varchar_pattern_ops);

CREATE TABLE deposit_product_rate_versions (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  product_id bigint NOT NULL REFERENCES deposit_products(id),
  annual_rate_basis_points integer NOT NULL CHECK (annual_rate_basis_points > 0 AND annual_rate_basis_points <= 10000),
  effective_from date NOT NULL,
  effective_to date,
  policy_version varchar(40) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(product_id,policy_version), CHECK (effective_to IS NULL OR effective_to >= effective_from)
);
CREATE INDEX idx_deposit_products_available ON deposit_products(deposit_type,status,effective_from,effective_to);
CREATE INDEX idx_deposit_rates_effective ON deposit_product_rate_versions(product_id,effective_from DESC);

CREATE TABLE deposit_accounts (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  reference varchar(40) NOT NULL UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  product_id bigint NOT NULL REFERENCES deposit_products(id),
  funding_account_id bigint NOT NULL REFERENCES bank_accounts(id),
  ledger_account_id bigint NOT NULL UNIQUE REFERENCES ledger_accounts(id),
  deposit_type varchar(12) NOT NULL CHECK (deposit_type IN ('FIXED','RECURRING')),
  status varchar(24) NOT NULL CHECK (status IN ('ACTIVE','MATURED','CLOSED','FAILED')),
  currency char(3) NOT NULL CHECK (currency='INR'),
  principal_minor bigint NOT NULL CHECK (principal_minor > 0),
  total_contributed_minor bigint NOT NULL DEFAULT 0 CHECK (total_contributed_minor >= 0),
  accrued_interest_minor bigint NOT NULL DEFAULT 0 CHECK (accrued_interest_minor >= 0),
  opening_date date NOT NULL,
  maturity_date date NOT NULL,
  next_due_date date,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK (maturity_date > opening_date)
);
CREATE INDEX idx_deposit_accounts_customer ON deposit_accounts(customer_id,status,maturity_date);

CREATE TABLE deposit_contract_terms (
  deposit_account_id bigint PRIMARY KEY REFERENCES deposit_accounts(id),
  product_code varchar(40) NOT NULL,
  product_name varchar(120) NOT NULL,
  product_version bigint NOT NULL,
  rate_policy_version varchar(40) NOT NULL,
  annual_rate_basis_points integer NOT NULL CHECK (annual_rate_basis_points > 0),
  tenure_months integer NOT NULL CHECK (tenure_months > 0),
  interest_method varchar(20) NOT NULL,
  day_count_convention varchar(20) NOT NULL,
  compounding_frequency varchar(20) NOT NULL,
  maturity_instruction varchar(30) NOT NULL CHECK (maturity_instruction IN ('CREDIT_LINKED_ACCOUNT')),
  installment_amount_minor bigint,
  projected_interest_minor bigint NOT NULL CHECK (projected_interest_minor >= 0),
  projected_maturity_minor bigint NOT NULL CHECK (projected_maturity_minor > 0),
  accepted_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE deposit_installment_occurrences (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  deposit_account_id bigint NOT NULL REFERENCES deposit_accounts(id),
  sequence_number integer NOT NULL CHECK (sequence_number > 0),
  scheduled_date date NOT NULL,
  amount_minor bigint NOT NULL CHECK (amount_minor > 0),
  currency char(3) NOT NULL CHECK(currency='INR'),
  status varchar(20) NOT NULL CHECK (status IN ('SCHEDULED','PROCESSING','COMPLETED','MISSED','FAILED')),
  idempotency_key varchar(100) NOT NULL UNIQUE,
  attempt_count integer NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
  claimed_at timestamptz, executed_at timestamptz,
  journal_id bigint UNIQUE REFERENCES journals(id),
  failure_reason varchar(100),
  created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(deposit_account_id,sequence_number)
);
CREATE INDEX idx_deposit_installments_due ON deposit_installment_occurrences(status,scheduled_date,id);

CREATE TABLE deposit_posting_references (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  deposit_account_id bigint NOT NULL REFERENCES deposit_accounts(id),
  operation_type varchar(30) NOT NULL,
  operation_key varchar(100) NOT NULL,
  journal_id bigint NOT NULL UNIQUE REFERENCES journals(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(deposit_account_id,operation_type,operation_key)
);
CREATE TABLE deposit_status_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  deposit_account_id bigint NOT NULL REFERENCES deposit_accounts(id),
  previous_status varchar(24), new_status varchar(24) NOT NULL,
  reason_code varchar(80), occurred_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO deposit_products(code,name,deposit_type,currency,minimum_amount_minor,maximum_amount_minor,minimum_tenure_months,maximum_tenure_months,installment_frequency,interest_method,compounding_frequency,premature_closure_allowed,renewal_allowed,grace_period_days,missed_installment_policy,status,effective_from)
VALUES
 ('SANDBOX_FD_GOLD','Novara Sandbox Fixed Deposit','FIXED','INR',100000,100000000,3,60,NULL,'COMPOUND_MONTHLY','MONTHLY',false,false,0,'MARK_MISSED','ACTIVE',DATE '2025-01-01'),
 ('SANDBOX_RD_GROWTH','Novara Sandbox Recurring Deposit','RECURRING','INR',50000,10000000,6,60,'MONTHLY','COMPOUND_MONTHLY','MONTHLY',false,false,5,'RETRY_DURING_GRACE','ACTIVE',DATE '2025-01-01');
INSERT INTO deposit_product_rate_versions(product_id,annual_rate_basis_points,effective_from,policy_version)
SELECT id,650,DATE '2025-01-01','SANDBOX-2025-01' FROM deposit_products;
