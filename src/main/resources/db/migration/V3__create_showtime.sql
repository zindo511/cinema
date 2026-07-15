-- =============================================
-- V3: Showtime Domain (showtime + showtime_seat)
-- Phụ thuộc: V1 (movie FK), V2 (auditorium FK, seat FK)
-- =============================================

-- 1. Suất chiếu
CREATE TABLE showtime (
    id              BIGSERIAL       PRIMARY KEY,
    movie_id        BIGINT          NOT NULL REFERENCES movie(id),
    auditorium_id   BIGINT          NOT NULL REFERENCES auditorium(id),
    start_time      TIMESTAMPTZ     NOT NULL,
    end_time        TIMESTAMPTZ     NOT NULL,
    base_price      DECIMAL(10,0)   NOT NULL CHECK (base_price > 0),
    status          SMALLINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,

    CHECK (end_time > start_time)
);

COMMENT ON COLUMN showtime.status IS '0=DRAFT, 1=OPEN, 2=COMPLETED, -1=CANCELLED';
COMMENT ON COLUMN showtime.end_time IS 'start_time + movie.duration + 15 phút dọn phòng (tính ở app layer)';
COMMENT ON COLUMN showtime.base_price IS 'Giá vé cơ bản, showtime_seat nhân hệ số theo seat_type';

CREATE INDEX idx_showtime_movie_id ON showtime(movie_id);
CREATE INDEX idx_showtime_auditorium_id ON showtime(auditorium_id);
CREATE INDEX idx_showtime_start_time ON showtime(start_time);
CREATE INDEX idx_showtime_status ON showtime(status);

-- Composite index: UC-03 (xem suất chiếu theo phim + ngày)
CREATE INDEX idx_showtime_movie_start ON showtime(movie_id, start_time);

-- Composite index: UC-10 (validate overlap cùng phòng)
CREATE INDEX idx_showtime_auditorium_time ON showtime(auditorium_id, start_time, end_time);

-- 2. Ghế cho từng suất chiếu
CREATE TABLE showtime_seat (
    id              BIGSERIAL       PRIMARY KEY,
    showtime_id     BIGINT          NOT NULL REFERENCES showtime(id),
    seat_id         BIGINT          NOT NULL REFERENCES seat(id),
    status          SMALLINT        NOT NULL DEFAULT 0,
    price           DECIMAL(10,0)   NOT NULL CHECK (price >= 0),
    held_at         TIMESTAMPTZ,
    version         INT             NOT NULL DEFAULT 0,

    UNIQUE(showtime_id, seat_id)
);

COMMENT ON COLUMN showtime_seat.status IS '0=AVAILABLE, 1=HELD, 2=BOOKED';
COMMENT ON COLUMN showtime_seat.held_at IS 'Thời điểm hold, dùng tính expiry (BT-03: seat hold TTL 10 phút)';
COMMENT ON COLUMN showtime_seat.version IS 'Optimistic locking version cho JPA @Version';
COMMENT ON COLUMN showtime_seat.price IS 'Giá snapshot = showtime.base_price × seat_type_multiplier';

-- Query phổ biến nhất: lấy seat map cho 1 suất chiếu (UC-04)
CREATE INDEX idx_showtime_seat_showtime_status ON showtime_seat(showtime_id, status);

-- Scheduled job: tìm ghế HELD đã hết hạn (BT-03)
CREATE INDEX idx_showtime_seat_held_at ON showtime_seat(held_at) WHERE status = 1;
