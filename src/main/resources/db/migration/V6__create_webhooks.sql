CREATE TABLE webhooks (
  merchant_id VARCHAR(255) PRIMARY KEY,
  url TEXT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


ALTER TABLE webhooks
ADD CONSTRAINT fk_webhooks_merchant
FOREIGN KEY (merchant_id) REFERENCES merchants(id);