-- =========================================================
-- V6: Refresh token for JWT token rotation
--
-- Stores hashed refresh tokens to support secure token
-- refresh flow without keeping the raw token in the database.
--
-- Depends on V5: User identity
-- =========================================================

CREATE TABLE refresh_token (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    token_hash      VARCHAR(64)     NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_revoked      SMALLINT        NOT NULL DEFAULT 0,

    CONSTRAINT ck_refresh_token_revoked
    CHECK (is_revoked IN (0, 1))
);

COMMENT ON TABLE refresh_token
    IS 'Hashed refresh tokens for JWT rotation; raw token is never stored';

COMMENT ON COLUMN refresh_token.token_hash IS 'SHA-256 hash of the raw refresh token';
COMMENT ON COLUMN refresh_token.is_revoked IS '0=ACTIVE, 1=REVOKED';

-- Fast lookup when validating a refresh token
CREATE UNIQUE INDEX uk_refresh_token_hash ON refresh_token(token_hash);

-- Find all tokens for a user (e.g. revoke all on password change)
CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);

-- Cleanup job: delete expired tokens
CREATE INDEX idx_refresh_token_expires_at ON refresh_token(expires_at) WHERE is_revoked = 0;