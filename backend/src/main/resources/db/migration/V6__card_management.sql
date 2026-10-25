CREATE TABLE card_products (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code varchar(30) NOT NULL UNIQUE,
  display_name varchar(80) NOT NULL,
  tier varchar(20) NOT NULL CHECK (tier IN ('CLASSIC','GOLD','PLATINUM')),
  funding_type varchar(20) NOT NULL CHECK (funding_type IN ('DEBIT','CREDIT_READY')),
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE cards (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  linked_account_id bigint REFERENCES bank_accounts(id),
  product_id bigint NOT NULL REFERENCES card_products(id),
  cardholder_name varchar(100) NOT NULL,
  masked_reference varchar(30) NOT NULL,
  status varchar(30) NOT NULL CHECK (status IN ('PENDING_ACTIVATION','ACTIVE','FROZEN','BLOCKED','EXPIRED','CLOSED')),
  expiry_month smallint CHECK (expiry_month BETWEEN 1 AND 12),
  expiry_year smallint CHECK (expiry_year BETWEEN 2020 AND 2200),
  processor_reference varchar(100) NOT NULL UNIQUE,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(customer_id,masked_reference)
);
CREATE INDEX idx_cards_customer_status ON cards(customer_id,status,created_at DESC);
CREATE TABLE card_controls (
  card_id bigint PRIMARY KEY REFERENCES cards(id),
  atm_enabled boolean NOT NULL DEFAULT true, online_enabled boolean NOT NULL DEFAULT true,
  pos_enabled boolean NOT NULL DEFAULT true, contactless_enabled boolean NOT NULL DEFAULT true,
  international_enabled boolean NOT NULL DEFAULT false,
  atm_limit_minor bigint NOT NULL DEFAULT 2000000 CHECK(atm_limit_minor>=0), online_limit_minor bigint NOT NULL DEFAULT 5000000 CHECK(online_limit_minor>=0),
  pos_limit_minor bigint NOT NULL DEFAULT 5000000 CHECK(pos_limit_minor>=0), contactless_limit_minor bigint NOT NULL DEFAULT 100000 CHECK(contactless_limit_minor>=0),
  daily_limit_minor bigint NOT NULL DEFAULT 7500000 CHECK(daily_limit_minor>0), version bigint NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE card_control_requests (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  card_id bigint NOT NULL REFERENCES cards(id), customer_id bigint NOT NULL REFERENCES customers(id),
  request_type varchar(30) NOT NULL, idempotency_key varchar(100) NOT NULL, request_hash char(64) NOT NULL,
  status varchar(20) NOT NULL CHECK(status IN ('PENDING','CONFIRMED','FAILED','UNKNOWN')),
  processor_reference varchar(100), failure_code varchar(60), created_at timestamptz NOT NULL DEFAULT now(), completed_at timestamptz,
  UNIQUE(customer_id,request_type,idempotency_key)
);
CREATE TABLE card_service_requests (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  card_id bigint NOT NULL REFERENCES cards(id), customer_id bigint NOT NULL REFERENCES customers(id),
  request_type varchar(20) NOT NULL CHECK(request_type IN ('LOST','DAMAGED','REPLACEMENT','SUPPORT')),
  status varchar(20) NOT NULL DEFAULT 'SUBMITTED' CHECK(status IN ('SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','CANCELLED')),
  notes varchar(240), created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE card_status_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, card_id bigint NOT NULL REFERENCES cards(id),
  previous_status varchar(30), new_status varchar(30) NOT NULL, reason_code varchar(60), processor_acknowledged boolean NOT NULL,
  occurred_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE card_transactions (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  card_id bigint NOT NULL REFERENCES cards(id), customer_id bigint NOT NULL REFERENCES customers(id),
  merchant_description varchar(140) NOT NULL, amount_minor bigint NOT NULL CHECK(amount_minor>0), currency char(3) NOT NULL,
  status varchar(20) NOT NULL CHECK(status IN ('PENDING','COMPLETED','REVERSED','DECLINED')),
  transaction_type varchar(20) NOT NULL CHECK(transaction_type IN ('ATM','ONLINE','POS','CONTACTLESS','INTERNATIONAL')),
  occurred_at timestamptz NOT NULL
);
CREATE INDEX idx_card_transactions_customer_card_date ON card_transactions(customer_id,card_id,occurred_at DESC);
INSERT INTO card_products(code,display_name,tier,funding_type) VALUES
 ('NOVARA_CLASSIC','Novara Classic','CLASSIC','DEBIT'),('NOVARA_GOLD','Novara Gold','GOLD','DEBIT'),('NOVARA_PLATINUM','Novara Platinum','PLATINUM','CREDIT_READY');
