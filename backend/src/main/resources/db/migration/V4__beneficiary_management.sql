CREATE TYPE beneficiary_status AS ENUM ('PENDING_VERIFICATION', 'ACTIVE', 'RESTRICTED', 'DEACTIVATED');

CREATE TABLE beneficiaries (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  customer_id bigint NOT NULL REFERENCES customers(id),
  name varchar(80) NOT NULL,
  nickname varchar(40),
  bank_name varchar(100) NOT NULL,
  account_reference_hash char(64) NOT NULL,
  masked_account varchar(32) NOT NULL,
  routing_code varchar(20) NOT NULL,
  transfer_method varchar(20) NOT NULL DEFAULT 'INTERNAL',
  status beneficiary_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
  favorite boolean NOT NULL DEFAULT false,
  verified_at timestamptz,
  cooling_until timestamptz,
  deactivated_at timestamptz,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT ck_beneficiary_name CHECK (btrim(name) <> ''),
  CONSTRAINT ck_beneficiary_routing CHECK (routing_code ~ '^[A-Z0-9-]{5,20}$'),
  CONSTRAINT ck_beneficiary_method CHECK (transfer_method IN ('INTERNAL', 'UPI', 'IMPS', 'NEFT', 'RTGS')),
  CONSTRAINT ck_beneficiary_deactivation CHECK ((status = 'DEACTIVATED') = (deactivated_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_beneficiary_customer_destination_active
  ON beneficiaries(customer_id, account_reference_hash, routing_code)
  WHERE status <> 'DEACTIVATED';
CREATE INDEX idx_beneficiary_customer_status_updated
  ON beneficiaries(customer_id, status, updated_at DESC);
CREATE INDEX idx_beneficiary_customer_favorite
  ON beneficiaries(customer_id, favorite, updated_at DESC) WHERE favorite;

CREATE TABLE beneficiary_verifications (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  beneficiary_id bigint NOT NULL REFERENCES beneficiaries(id),
  status varchar(30) NOT NULL,
  provider_reference varchar(120),
  failure_code varchar(60),
  attempted_at timestamptz NOT NULL DEFAULT now(),
  completed_at timestamptz,
  CONSTRAINT ck_beneficiary_verification_status CHECK (status IN ('PENDING', 'VERIFIED', 'FAILED', 'EXPIRED')),
  CONSTRAINT ck_beneficiary_verification_result CHECK (
    (status = 'PENDING' AND completed_at IS NULL) OR
    (status <> 'PENDING' AND completed_at IS NOT NULL)
  )
);
CREATE INDEX idx_beneficiary_verification_beneficiary
  ON beneficiary_verifications(beneficiary_id, attempted_at DESC);

