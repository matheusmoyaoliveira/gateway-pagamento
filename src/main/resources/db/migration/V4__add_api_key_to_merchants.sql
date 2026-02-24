-- 1) adiciona coluna
ALTER TABLE merchants
ADD COLUMN api_key VARCHAR(255);

-- 2) seta api_key pros exemplos
UPDATE merchants SET api_key = 'sk_live_lojaX_123' WHERE id = 'lojaX';
UPDATE merchants SET api_key = 'sk_live_loja2_456' WHERE id = 'loja2';

-- 2.1) garante que nenhum merchant fica sem chave
-- gera uma chave "quebra-galho" pra qualquer NULL restante
UPDATE merchants
SET api_key = 'sk_live_' || md5(random()::text || clock_timestamp()::text)
WHERE api_key IS NULL;

-- 3) agora sim trava
ALTER TABLE merchants
ALTER COLUMN api_key SET NOT NULL;

-- 4) unique
ALTER TABLE merchants
ADD CONSTRAINT merchants_api_key_unique UNIQUE (api_key);
