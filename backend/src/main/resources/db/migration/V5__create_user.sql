-- =========================================================
-- V5: User identity and booking-customer relationship
--
-- Creates the users table for authentication, authorization,
-- and account status management.
--
-- Adds a foreign key from booking.customer_id to users.id.
--
-- Depends on V4: Booking domain
-- =========================================================

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(100)    NOT NULL,
    role            SMALLINT        NOT NULL DEFAULT 0,
    status          SMALLINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,

    CONSTRAINT ck_users_role
    CHECK (role IN (0, 1, 2)),

    CONSTRAINT ck_users_status
    CHECK (status IN (-1, 0, 1))
);

COMMENT ON TABLE users
    IS 'Application users used for authentication, authorization, and booking ownership';

COMMENT ON COLUMN users.status IS '0=ACTIVE, 1=LOCKED, -1=DELETED';

COMMENT ON COLUMN users.role IS '0=USER, 1=STAFF, 2=ADMIN';

-- Enforce case-insensitive email uniqueness.
CREATE UNIQUE INDEX uk_users_email_ci ON users(LOWER(email));

-- add reference to booking.customer_id
ALTER TABLE booking ADD CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES users(id);

COMMENT ON COLUMN booking.customer_id
    IS 'Customer who owns the booking; references users.id';