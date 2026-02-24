ALTER TABLE idempotency_keys
ADD COLUMN merchant_id VARCHAR(255);


UPDATE idempotency_keys
SET merchant_id = 'loja2'
WHERE merchant_id IS NULL;


ALTER TABLE idempotency_keys
ALTER COLUMN merchant_id SET NOT NULL;


ALTER TABLE idempotency_keys
DROP CONSTRAINT idempotency_keys_pkey;

ALTER TABLE idempotency_keys
ADD CONSTRAINT idempotency_keys_pkey PRIMARY KEY (merchant_id, key);
