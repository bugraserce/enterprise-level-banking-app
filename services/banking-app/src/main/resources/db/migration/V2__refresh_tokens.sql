-- V2: opaque refresh tokens. The plain token is shown once and forgotten;
-- only its SHA-256 hash sleeps here. Rotation = old row revoked, new row written.
CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_refresh_user ON auth.refresh_tokens(user_id);
COMMENT ON TABLE auth.refresh_tokens IS 'Identity-owned. One row per live refresh token.';