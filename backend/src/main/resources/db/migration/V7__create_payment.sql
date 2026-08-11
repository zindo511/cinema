-- =========================================================
-- V7: Payment domain
--
-- A booking can have multiple payment attempts. The
-- payment_reference identifies one logical payment attempt,
-- provides request idempotency, and is sent to the provider
-- as the merchant transaction reference.
--
-- Depends on V4: Booking domain
-- =========================================================

CREATE TABLE payment (
    id                      BIGSERIAL       PRIMARY KEY,
    booking_id              BIGINT          NOT NULL REFERENCES booking(id),

    payment_reference       VARCHAR(100)    NOT NULL,
    provider                VARCHAR(30)     NOT NULL,
    status                  SMALLINT        NOT NULL DEFAULT 0,

    amount                  DECIMAL(12,0)   NOT NULL,

    provider_transaction_no VARCHAR(100),
    response_code           VARCHAR(20),
    bank_code               VARCHAR(30),
    failure_reason          VARCHAR(500),
    paid_at                 TIMESTAMPTZ,

    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by              BIGINT,
    updated_by              BIGINT,

    CONSTRAINT uk_payment_reference
        UNIQUE (payment_reference),

    CONSTRAINT ck_payment_status
        CHECK (status IN (-1, 0, 1)),

    CONSTRAINT ck_payment_amount
        CHECK (amount > 0)
);

COMMENT ON TABLE payment
    IS 'Payment attempts made for bookings';

COMMENT ON COLUMN payment.payment_reference
    IS 'Merchant reference used for request idempotency and provider callback correlation';

COMMENT ON COLUMN payment.status
    IS '-1=FAILED, 0=PENDING, 1=SUCCESS';

COMMENT ON COLUMN payment.provider_transaction_no
    IS 'Transaction number assigned by the payment provider';

CREATE INDEX idx_payment_booking_id
    ON payment(booking_id);

