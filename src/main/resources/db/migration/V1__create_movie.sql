-- =============================================
-- V1: Movie Domain (movie + genre + movie_genre)
-- =============================================

-- 1. Bảng phim
CREATE TABLE movie (
    id                  BIGSERIAL       PRIMARY KEY,
    title               VARCHAR(255)    NOT NULL,
    description         TEXT,
    duration_minutes    INT             NOT NULL CHECK (duration_minutes > 0),
    poster_url          VARCHAR(500),
    trailer_url         VARCHAR(500),
    release_date        DATE,
    rating              DECIMAL(2,1)    DEFAULT 0,
    status              SMALLINT        NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT
);

COMMENT ON COLUMN movie.status IS '0=COMING_SOON, 1=NOW_SHOWING, 2=ENDED, -1=DELETED';
COMMENT ON COLUMN movie.duration_minutes IS 'Thời lượng phim tính bằng phút';
COMMENT ON COLUMN movie.rating IS 'Điểm đánh giá 0.0 - 9.9';

CREATE INDEX idx_movie_status ON movie(status);

-- 2. Bảng thể loại phim (master data)
CREATE TABLE genre (
    id      BIGSERIAL       PRIMARY KEY,
    name    VARCHAR(50)     NOT NULL UNIQUE
);

COMMENT ON TABLE genre IS 'Master data: Action, Comedy, Horror, Sci-Fi, Romance, Animation...';

-- 3. Bảng trung gian movie <-> genre (N-N)
CREATE TABLE movie_genre (
    movie_id    BIGINT      NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    genre_id    BIGINT      NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

-- Index chiều ngược: tìm movies theo genre (PK đã index movie_id)
CREATE INDEX idx_movie_genre_genre_id ON movie_genre(genre_id);