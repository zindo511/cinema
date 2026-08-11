CREATE TABLE refund (
    id              BIGSERIAL       PRIMARY KEY,
    payment_id      BIGINT          NOT NULL,
    amount          DECIMAL(12, 2)  NOT NULL,
    status          SMALLINT        NOT NULL DEFAULT 0,
    reason          VARCHAR(500),
    provider_refund_id VARCHAR(100),
    response_code   VARCHAR(20),
    failure_reason  VARCHAR(500),
    refunded_at     TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payment (id)
);

CREATE INDEX idx_refund_payment_id ON refund (payment_id);
CREATE INDEX idx_refund_status ON refund (status);
