CREATE TABLE merchants (
    id          VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT merchants_status_check CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

INSERT INTO merchants (id, name, status)
VALUES
    ('lojax', 'Loja X', 'ACTIVE'),
    ('loja2', 'Loja 2', 'ACTIVE');
