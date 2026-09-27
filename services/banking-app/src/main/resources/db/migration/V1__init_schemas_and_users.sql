-- V1: schema-per-domain foundations + identity users table.
-- Rule: app code never touches another domain's schema directly.
-- Schemas: auth (identity), customer (KYC profile), banking (accounts + transfers for now).

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS customer;
CREATE SCHEMA IF NOT EXISTS banking;

-- gen_random_uuid() is built-in since Postgres 13, no extension needed.
CREATE TABLE IF NOT EXISTS auth.users (
    -- UUID PK: non-sequential, safe to expose in URLs/logs (vs incrementing ids).
   id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Login key. CITEXT would give case-insensitive login; plain VARCHAR keeps V1 simple,
    -- uniqueness enforced exactly as typed. Normalized on write by the service layer.
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    -- BCrypt hash ($2a$...), never a plain password. 60 chars today, 255 for future algorithms.
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
    );

COMMENT ON TABLE auth.users IS 'Identity domain: login credentials. Owned only by identity package.';
COMMENT ON COLUMN auth.users.password_hash IS 'BCrypt hash. NEVER log or return via API.';