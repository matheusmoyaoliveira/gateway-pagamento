CREATE TABLE webhook_events (
  id VARCHAR(255) PRIMARY KEY,
  merchant_id VARCHAR(255) NOT NULL,
  payment_id VARCHAR(255) NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  payload TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  next_retry_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_error TEXT
);

ALTER TABLE webhook_events
ADD CONSTRAINT fk_webhook_events_merchant
FOREIGN KEY (merchant_id) REFERENCES merchants(id);

ALTER TABLE webhook_events
ADD CONSTRAINT fk_webhook_events_payment
FOREIGN KEY (payment_id) REFERENCES payments(id);

CREATE INDEX idx_webhook_events_status_next_retry
ON webhook_events(status, next_retry_at);

CREATE INDEX idx_webhook_events_merchant
ON webhook_events(merchant_id);