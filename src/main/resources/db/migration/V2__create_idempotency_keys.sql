CREATE TABLE idempotency_keys (
    key VARCHAR(100) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    request_hash TEXT NOT NULL,
    response_body TEXT NOT NULL,
    response_status INTEGER NOT NULL
);
