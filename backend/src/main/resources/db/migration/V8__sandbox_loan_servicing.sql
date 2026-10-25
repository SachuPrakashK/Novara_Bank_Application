CREATE TABLE loan_products (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  code varchar(40) NOT NULL UNIQUE,
  name varchar(120) NOT NULL,
  category varchar(24) NOT NULL CHECK(category IN ('PERSONAL','VEHICLE','HOME','EDUCATION')),
  currency char(3) NOT NULL CHECK(currency='INR'),
  minimum_principal_minor bigint NOT NULL CHECK(minimum_principal_minor>0),
  maximum_principal_minor bigint NOT NULL CHECK(maximum_principal_minor>=minimum_principal_minor),
  minimum_tenure_months integer NOT NULL CHECK(minimum_tenure_months>0),
  maximum_tenure_months integer NOT NULL CHECK(maximum_tenure_months>=minimum_tenure_months),
  repayment_frequency varchar(12) NOT NULL CHECK(repayment_frequency='MONTHLY'),
  status varchar(12) NOT NULL CHECK(status IN ('ACTIVE','INACTIVE')),
  effective_from date NOT NULL,effective_to date,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK(effective_to IS NULL OR effective_to>=effective_from)
);
CREATE TABLE loan_product_versions (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  product_id bigint NOT NULL REFERENCES loan_products(id),
  policy_version varchar(40) NOT NULL,
  annual_rate_basis_points integer NOT NULL CHECK(annual_rate_basis_points>=0 AND annual_rate_basis_points<=10000),
  rate_type varchar(16) NOT NULL CHECK(rate_type='FIXED'),
  calculation_method varchar(32) NOT NULL CHECK(calculation_method='REDUCING_BALANCE_EMI'),
  day_count_convention varchar(20) NOT NULL CHECK(day_count_convention='MONTHLY_PERIODIC'),
  rounding_mode varchar(16) NOT NULL CHECK(rounding_mode='HALF_EVEN'),
  processing_fee_minor bigint NOT NULL DEFAULT 0 CHECK(processing_fee_minor>=0),
  partial_payment_allowed boolean NOT NULL DEFAULT false,
  prepayment_allowed boolean NOT NULL DEFAULT false,
  effective_from date NOT NULL,effective_to date,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(product_id,policy_version),CHECK(effective_to IS NULL OR effective_to>=effective_from)
);
CREATE INDEX idx_loan_products_available ON loan_products(status,effective_from,effective_to);
CREATE INDEX idx_loan_product_versions_effective ON loan_product_versions(product_id,effective_from DESC);

CREATE TABLE loan_quotes (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  product_id bigint NOT NULL REFERENCES loan_products(id),
  product_version_id bigint NOT NULL REFERENCES loan_product_versions(id),
  principal_minor bigint NOT NULL CHECK(principal_minor>0),
  currency char(3) NOT NULL CHECK(currency='INR'),
  tenure_months integer NOT NULL CHECK(tenure_months>0),
  annual_rate_basis_points integer NOT NULL CHECK(annual_rate_basis_points>=0),
  emi_minor bigint NOT NULL CHECK(emi_minor>0),
  total_interest_minor bigint NOT NULL CHECK(total_interest_minor>=0),
  total_repayment_minor bigint NOT NULL CHECK(total_repayment_minor>=principal_minor),
  processing_fee_minor bigint NOT NULL CHECK(processing_fee_minor>=0),
  first_installment_date date NOT NULL,maturity_date date NOT NULL,
  expires_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  CHECK(maturity_date>=first_installment_date)
);
CREATE INDEX idx_loan_quotes_customer_expiry ON loan_quotes(customer_id,expires_at DESC);
CREATE TABLE loan_quote_installments (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  quote_id bigint NOT NULL REFERENCES loan_quotes(id) ON DELETE CASCADE,
  sequence_number integer NOT NULL CHECK(sequence_number>0),
  due_date date NOT NULL,
  opening_principal_minor bigint NOT NULL CHECK(opening_principal_minor>=0),
  scheduled_principal_minor bigint NOT NULL CHECK(scheduled_principal_minor>0),
  scheduled_interest_minor bigint NOT NULL CHECK(scheduled_interest_minor>=0),
  scheduled_fee_minor bigint NOT NULL DEFAULT 0 CHECK(scheduled_fee_minor>=0),
  total_due_minor bigint NOT NULL CHECK(total_due_minor>0),
  closing_principal_minor bigint NOT NULL CHECK(closing_principal_minor>=0),
  UNIQUE(quote_id,sequence_number)
);

CREATE TABLE loan_applications (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  reference varchar(40) NOT NULL UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  product_id bigint NOT NULL REFERENCES loan_products(id),
  quote_id bigint NOT NULL UNIQUE REFERENCES loan_quotes(id),
  status varchar(40) NOT NULL CHECK(status IN ('SUBMITTED','UNDER_REVIEW','ADDITIONAL_INFORMATION_REQUIRED','APPROVED','REJECTED','CANCELLED','EXPIRED')),
  purpose_category varchar(24) NOT NULL CHECK(purpose_category IN ('PERSONAL','VEHICLE','HOME','EDUCATION','OTHER')),
  customer_declaration boolean NOT NULL CHECK(customer_declaration),
  idempotency_key varchar(100) NOT NULL,
  request_hash char(64) NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  submitted_at timestamptz NOT NULL DEFAULT now(),updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(customer_id,idempotency_key)
);
CREATE INDEX idx_loan_applications_customer_status ON loan_applications(customer_id,status,submitted_at DESC);
CREATE TABLE loan_status_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  application_id bigint NOT NULL REFERENCES loan_applications(id),
  previous_status varchar(40),new_status varchar(40) NOT NULL,
  reason_code varchar(80),occurred_at timestamptz NOT NULL DEFAULT now()
);

-- Reserved for future approved booking. No Phase F workflow inserts these rows.
CREATE TABLE loan_accounts (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  application_id bigint NOT NULL UNIQUE REFERENCES loan_applications(id),customer_id bigint NOT NULL REFERENCES customers(id),
  reference varchar(40) NOT NULL UNIQUE,status varchar(24) NOT NULL CHECK(status IN ('PENDING_BOOKING','ACTIVE','DELINQUENT','CLOSED','CANCELLED','DEFAULTED')),
  original_principal_minor bigint NOT NULL CHECK(original_principal_minor>0),outstanding_principal_minor bigint NOT NULL CHECK(outstanding_principal_minor>=0),
  accrued_interest_minor bigint NOT NULL DEFAULT 0 CHECK(accrued_interest_minor>=0),currency char(3) NOT NULL CHECK(currency='INR'),
  version bigint NOT NULL DEFAULT 0,created_at timestamptz NOT NULL DEFAULT now(),updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO loan_products(code,name,category,currency,minimum_principal_minor,maximum_principal_minor,minimum_tenure_months,maximum_tenure_months,repayment_frequency,status,effective_from) VALUES
 ('SANDBOX_PERSONAL','Novara Sandbox Personal Loan','PERSONAL','INR',500000,50000000,6,60,'MONTHLY','ACTIVE',DATE '2025-01-01'),
 ('SANDBOX_EDUCATION','Novara Sandbox Education Loan','EDUCATION','INR',1000000,100000000,12,120,'MONTHLY','ACTIVE',DATE '2025-01-01');
INSERT INTO loan_product_versions(product_id,policy_version,annual_rate_basis_points,rate_type,calculation_method,day_count_convention,rounding_mode,processing_fee_minor,partial_payment_allowed,prepayment_allowed,effective_from)
SELECT id,'SANDBOX-2025-01',CASE WHEN code='SANDBOX_EDUCATION' THEN 0 ELSE 1200 END,'FIXED','REDUCING_BALANCE_EMI','MONTHLY_PERIODIC','HALF_EVEN',0,false,false,DATE '2025-01-01' FROM loan_products;
