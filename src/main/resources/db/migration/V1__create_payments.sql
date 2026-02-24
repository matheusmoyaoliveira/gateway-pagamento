-- Criação da tabela payments
CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    amount BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    currency VARCHAR(3) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT payments_status_check CHECK (status IN (
        'CREATED', 'AUTHORIZED', 'CAPTURED', 'REFUNDED', 'CANCELED', 'DECLINED'
    ))
);