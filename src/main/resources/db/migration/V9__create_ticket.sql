CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(36) NOT NULL UNIQUE,
    booking_seat_id BIGINT NOT NULL UNIQUE,
    status SMALLINT NOT NULL,
    scanned_at TIMESTAMP WITH TIME ZONE,
    scanned_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_booking_seat FOREIGN KEY (booking_seat_id) REFERENCES booking_seat (id),
    CONSTRAINT fk_ticket_user FOREIGN KEY (scanned_by) REFERENCES "users" (id)
);

CREATE INDEX idx_ticket_code ON ticket (ticket_code);
