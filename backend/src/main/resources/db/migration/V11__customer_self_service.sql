ALTER TABLE customers
  ADD COLUMN preferred_display_name varchar(80),
  ADD COLUMN registered_email varchar(254),
  ADD COLUMN email_verified boolean NOT NULL DEFAULT false,
  ADD COLUMN registered_mobile varchar(32),
  ADD COLUMN mobile_verified boolean NOT NULL DEFAULT false,
  ADD COLUMN postal_address varchar(500),
  ADD COLUMN version bigint NOT NULL DEFAULT 0;

CREATE TABLE customer_preferences (
  customer_id bigint PRIMARY KEY REFERENCES customers(id),
  language varchar(10) NOT NULL DEFAULT 'en' CHECK (language IN ('en')),
  theme varchar(10) NOT NULL DEFAULT 'SYSTEM' CHECK (theme IN ('SYSTEM','LIGHT','DARK')),
  date_format varchar(20) NOT NULL DEFAULT 'DD_MMM_YYYY' CHECK (date_format IN ('DD_MMM_YYYY','DD_MM_YYYY')),
  reduced_motion boolean NOT NULL DEFAULT false,
  version bigint NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TYPE service_request_status AS ENUM ('SUBMITTED','UNDER_REVIEW','ADDITIONAL_INFORMATION_REQUIRED','RESOLVED','REJECTED','CANCELLED');
CREATE TABLE service_requests (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  category varchar(40) NOT NULL CHECK (category IN ('ACCOUNT_INFORMATION','STATEMENT_ASSISTANCE','CONTACT_UPDATE_ASSISTANCE','CARD_SUPPORT','GENERAL_ACCOUNT_SUPPORT')),
  description varchar(500) NOT NULL,
  status service_request_status NOT NULL DEFAULT 'SUBMITTED',
  customer_visible_response varchar(1000),
  idempotency_key varchar(100) NOT NULL,
  request_hash char(64) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(customer_id,idempotency_key)
);
CREATE INDEX idx_service_requests_customer_created ON service_requests(customer_id,created_at DESC);
CREATE TABLE service_request_status_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  service_request_id bigint NOT NULL REFERENCES service_requests(id),
  status service_request_status NOT NULL,
  customer_visible_message varchar(1000),
  occurred_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE notification_preferences (
  customer_id bigint PRIMARY KEY REFERENCES customers(id),
  transaction_activity boolean NOT NULL DEFAULT true,
  statements boolean NOT NULL DEFAULT true,
  deposits boolean NOT NULL DEFAULT true,
  loan_applications boolean NOT NULL DEFAULT true,
  service_requests boolean NOT NULL DEFAULT true,
  version bigint NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE customer_notifications (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  type varchar(40) NOT NULL,
  title varchar(120) NOT NULL,
  message varchar(300) NOT NULL,
  navigation_target varchar(240),
  source_event_id uuid,
  read_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(customer_id,source_event_id)
);
CREATE INDEX idx_notifications_customer_created ON customer_notifications(customer_id,created_at DESC);

