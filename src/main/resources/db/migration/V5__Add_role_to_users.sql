ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CLIENT';

ALTER TABLE users ADD CONSTRAINT chk_users_role
    CHECK (role IN ('ADMIN', 'PROVIDER', 'CLIENT'));

-- Usuários que já possuem serviços cadastrados passam a ser prestadores
UPDATE users SET role = 'PROVIDER'
WHERE id IN (SELECT DISTINCT provider_id FROM services);
