-- =============================================
-- V2: Cinema Domain (cinema + auditorium + seat)
-- =============================================

-- 1. Bảng rạp
CREATE TABLE cinema (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    city            VARCHAR(100)    NOT NULL,
    district        VARCHAR(100),
    address         VARCHAR(500)    NOT NULL,
    phone           VARCHAR(20),
    status          SMALLINT        NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT
);

COMMENT ON COLUMN cinema.status IS '1=ACTIVE, 0=INACTIVE, -1=DELETED';

CREATE INDEX idx_cinema_city ON cinema(city);
CREATE INDEX idx_cinema_status ON cinema(status);

-- 2. Phòng chiếu
CREATE TABLE auditorium (
    id              BIGSERIAL       PRIMARY KEY,
    cinema_id       BIGINT          NOT NULL REFERENCES cinema(id),
    name            VARCHAR(100)    NOT NULL,
    screen_type     SMALLINT        NOT NULL DEFAULT 0,
    total_rows      INT             NOT NULL CHECK (total_rows > 0),
    total_columns   INT             NOT NULL CHECK (total_columns > 0),
    status          SMALLINT        NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,

    UNIQUE(cinema_id, name)
);

COMMENT ON COLUMN auditorium.screen_type IS '0=SCREEN_2D, 1=SCREEN_3D, 2=IMAX, 3=4DX';
COMMENT ON COLUMN auditorium.status IS '1=ACTIVE, 0=MAINTENANCE, -1=DELETED';
COMMENT ON COLUMN auditorium.total_rows IS 'Số hàng ghế (grid height) — dùng để render seat map';
COMMENT ON COLUMN auditorium.total_columns IS 'Số cột ghế (grid width) — dùng để render seat map';

CREATE INDEX idx_auditorium_cinema_id ON auditorium(cinema_id);

-- 3. Ghế vật lý
CREATE TABLE seat (
    id              BIGSERIAL       PRIMARY KEY,
    auditorium_id   BIGINT          NOT NULL REFERENCES auditorium(id),
    seat_row        VARCHAR(2)      NOT NULL,
    seat_number     INT             NOT NULL CHECK (seat_number > 0),
    seat_type       SMALLINT        NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,

    UNIQUE(auditorium_id, seat_row, seat_number)
);

COMMENT ON COLUMN seat.seat_type IS '0=STANDARD, 1=VIP, 2=COUPLE';
COMMENT ON COLUMN seat.seat_row IS 'Ký hiệu hàng: A, B, C... tối đa Z, AA';
COMMENT ON COLUMN seat.seat_number IS 'Vị trí cột BẮT ĐẦU. Ghế Couple bắt đầu ở cột này và chiếm thêm cột kế tiếp';
COMMENT ON COLUMN seat.is_active IS 'FALSE = ghế hỏng/bảo trì (BT-19)';

CREATE INDEX idx_seat_auditorium_id ON seat(auditorium_id);
