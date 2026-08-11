-- =============================================
-- V4: Booking domain (booking + booking_seat)
-- Depends on V3 (showtime + showtime_seat)
-- =============================================

CREATE TABLE booking (
    id                  BIGSERIAL       PRIMARY KEY,
    booking_code        VARCHAR(20)     NOT NULL,
    customer_id         BIGINT          NOT NULL,
    showtime_id         BIGINT          NOT NULL REFERENCES showtime(id),
    status              SMALLINT        NOT NULL DEFAULT 0,
    total_amount        DECIMAL(12,0)   NOT NULL CHECK (total_amount >= 0),
    expires_at          TIMESTAMPTZ     NOT NULL,
    idempotency_key     UUID            NOT NULL,
    request_hash        VARCHAR(64)     NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,

    CONSTRAINT uk_booking_code UNIQUE (booking_code),
    CONSTRAINT uk_booking_customer_idempotency UNIQUE (customer_id, idempotency_key)
);

COMMENT ON COLUMN booking.status IS '-1=CANCELLED, 0=PENDING, 1=CONFIRMED, 2=EXPIRED';
COMMENT ON COLUMN booking.customer_id IS 'Identity supplied by CurrentCustomerPort; no user FK until the identity module exists';
COMMENT ON COLUMN booking.request_hash IS 'SHA-256 of the normalized booking request';

CREATE INDEX idx_booking_customer_created ON booking(customer_id, created_at DESC);
CREATE INDEX idx_booking_showtime_id ON booking(showtime_id);
CREATE INDEX idx_booking_pending_expires_at ON booking(expires_at) WHERE status = 0;

CREATE TABLE booking_seat (
    id                  BIGSERIAL       PRIMARY KEY,
    booking_id          BIGINT          NOT NULL REFERENCES booking(id),
    showtime_seat_id    BIGINT          NOT NULL REFERENCES showtime_seat(id),
    seat_label          VARCHAR(10)     NOT NULL,
    seat_type           SMALLINT        NOT NULL,
    price               DECIMAL(10,0)   NOT NULL CHECK (price >= 0),

    CONSTRAINT uk_booking_seat UNIQUE (booking_id, showtime_seat_id)
);

COMMENT ON TABLE booking_seat IS 'Immutable seat label, type, and price snapshots captured when a booking is created';
COMMENT ON COLUMN booking_seat.seat_type IS '0=STANDARD, 1=VIP, 2=COUPLE';
