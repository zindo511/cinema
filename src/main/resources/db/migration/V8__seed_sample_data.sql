-- =========================================================
-- V8: Rich sample data for local development and demos
--
-- The seed is deliberately generated from compact input tables so a fresh
-- database gets a realistic amount of relational data without maintaining
-- tens of thousands of literal INSERT statements.
--
-- Audit value -8 identifies rows created by this migration. Operational
-- tables still use valid lifecycle combinations (for example, only active
-- bookings keep HELD/BOOKED showtime seats).
-- =========================================================

-- ---------------------------------------------------------
-- 1. Genres and movies
-- ---------------------------------------------------------

INSERT INTO genre (name)
VALUES
    ('Action'),
    ('Adventure'),
    ('Animation'),
    ('Comedy'),
    ('Crime'),
    ('Documentary'),
    ('Drama'),
    ('Family'),
    ('Fantasy'),
    ('Horror'),
    ('Mystery'),
    ('Romance'),
    ('Sci-Fi'),
    ('Thriller'),
    ('War')
ON CONFLICT (name) DO NOTHING;

CREATE TEMP TABLE seed_movie_input (
    seed_key           VARCHAR(40) PRIMARY KEY,
    title              VARCHAR(255) NOT NULL,
    description        TEXT NOT NULL,
    duration_minutes   INT NOT NULL,
    release_offset     INT NOT NULL,
    rating             DECIMAL(2,1) NOT NULL,
    status             SMALLINT NOT NULL,
    genre_names        TEXT[] NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_movie_input
    (seed_key, title, description, duration_minutes, release_offset, rating, status, genre_names)
VALUES
    ('midnight_saigon', 'Sài Gòn Lúc Nửa Đêm', 'Một nữ phóng viên lần theo chuỗi vụ mất tích bí ẩn giữa thành phố không ngủ.', 126, -45, 8.4, 1, ARRAY['Crime','Mystery','Thriller']),
    ('red_river', 'Dòng Sông Đỏ', 'Hành trình trở về quê nhà buộc hai anh em đối mặt với bí mật của gia đình.', 118, -38, 8.1, 1, ARRAY['Drama','Family']),
    ('cloud_hunters', 'Những Kẻ Săn Mây', 'Đội phi công trẻ bước vào vùng bão để cứu một trạm nghiên cứu biệt lập.', 142, -32, 8.6, 1, ARRAY['Action','Adventure','Drama']),
    ('last_signal', 'Tín Hiệu Cuối Cùng', 'Một tín hiệu từ không gian sâu làm đảo lộn cuộc sống trên trạm quỹ đạo.', 134, -28, 8.8, 1, ARRAY['Sci-Fi','Mystery','Thriller']),
    ('summer_balcony', 'Mùa Hè Trên Ban Công', 'Hai người hàng xóm tìm thấy tình yêu qua những bản nhạc phát mỗi chiều.', 108, -24, 7.9, 1, ARRAY['Romance','Comedy']),
    ('tiny_guardians', 'Biệt Đội Hạt Tiêu', 'Những linh vật tí hon bảo vệ khu vườn trước cỗ máy xây dựng khổng lồ.', 96, -20, 8.2, 1, ARRAY['Animation','Family','Adventure']),
    ('mirror_house', 'Ngôi Nhà Trong Gương', 'Một gia đình chuyển tới căn biệt thự nơi hình phản chiếu luôn đi trước họ một bước.', 112, -18, 7.8, 1, ARRAY['Horror','Mystery']),
    ('zero_hour', 'Giờ Thứ Không', 'Đội đặc nhiệm có sáu mươi phút để ngăn một cuộc tấn công mạng trên toàn quốc.', 137, -15, 8.3, 1, ARRAY['Action','Thriller']),
    ('paper_moon', 'Trăng Giấy', 'Cô bé làm đèn lồng cùng ông ngoại để cứu lễ hội truyền thống của thị trấn.', 101, -13, 8.5, 1, ARRAY['Family','Drama']),
    ('silent_witness', 'Nhân Chứng Im Lặng', 'Một nghệ sĩ đường phố vô tình nắm giữ manh mối của vụ án lớn nhất thành phố.', 121, -11, 8.0, 1, ARRAY['Crime','Drama','Mystery']),
    ('dragon_gate', 'Cổng Rồng', 'Nhóm khảo cổ đánh thức người canh giữ cổ xưa trong chuyến thám hiểm hang động.', 148, -9, 8.7, 1, ARRAY['Fantasy','Adventure','Action']),
    ('second_chance', 'Ngày Mai Mình Lại Yêu', 'Một cặp đôi được sống lại ngày chia tay để hiểu điều họ thật sự mong muốn.', 109, -7, 7.7, 1, ARRAY['Romance','Fantasy','Comedy']),
    ('deep_blue', 'Vực Xanh', 'Đoàn thám hiểm đại dương phát hiện một hệ sinh thái chưa từng được biết đến.', 116, -6, 8.9, 1, ARRAY['Documentary','Adventure']),
    ('laughing_detective', 'Thám Tử Hay Cười', 'Một thám tử vụng về phá án bằng những chi tiết chẳng ai để ý.', 104, -5, 7.6, 1, ARRAY['Comedy','Crime']),
    ('iron_birds', 'Cánh Chim Sắt', 'Phi đội vận tải thực hiện nhiệm vụ giải cứu dân thường trong vùng chiến sự.', 151, -3, 8.4, 1, ARRAY['War','Action','Drama']),
    ('neon_city', 'Thành Phố Neon', 'Nữ kỹ sư truy tìm bản sao ký ức của mình trong một đô thị tương lai.', 139, -1, 8.5, 1, ARRAY['Sci-Fi','Action','Mystery']),

    ('whale_song', 'Khúc Ca Của Cá Voi', 'Bộ phim theo chân các nhà sinh học ghi lại hành trình xuyên đại dương của cá voi xanh.', 103, 7, 0.0, 0, ARRAY['Documentary','Family']),
    ('golden_ticket', 'Tấm Vé Màu Vàng', 'Bốn người xa lạ cùng trúng một chuyến tàu đặc biệt đi qua những miền ký ức.', 115, 12, 0.0, 0, ARRAY['Fantasy','Drama','Adventure']),
    ('ghost_frequency', 'Tần Số Ma', 'Nhóm phát thanh sinh viên nhận được cuộc gọi trực tiếp từ ba mươi năm trước.', 110, 18, 0.0, 0, ARRAY['Horror','Mystery','Thriller']),
    ('orbit_9', 'Quỹ Đạo Số 9', 'Phi hành đoàn cuối cùng tìm đường trở về sau khi Trái Đất mất liên lạc.', 145, 25, 0.0, 0, ARRAY['Sci-Fi','Adventure','Drama']),
    ('wedding_plan_b', 'Kế Hoạch Đám Cưới B', 'Hai gia đình đối đầu khi cô dâu chú rể bí mật đổi toàn bộ kế hoạch.', 107, 32, 0.0, 0, ARRAY['Comedy','Romance']),
    ('forest_spirits', 'Những Người Bạn Trong Rừng', 'Cậu bé lạc đường kết bạn với các linh hồn bảo vệ khu rừng.', 94, 40, 0.0, 0, ARRAY['Animation','Family','Fantasy']),
    ('black_tide', 'Thủy Triều Đen', 'Đội cứu hộ chạy đua ngăn thảm họa môi trường lan tới thành phố ven biển.', 132, 48, 0.0, 0, ARRAY['Action','Drama','Thriller']),
    ('letters_to_winter', 'Thư Gửi Mùa Đông', 'Những lá thư thất lạc đưa hai người trẻ tới cùng một thị trấn trên núi.', 111, 55, 0.0, 0, ARRAY['Romance','Drama']),

    ('old_quarter', 'Chuyện Ở Phố Cũ', 'Ba thế hệ cùng gìn giữ một tiệm ảnh nhỏ giữa khu phố đang đổi thay.', 117, -420, 8.2, 2, ARRAY['Drama','Family']),
    ('storm_front', 'Trước Cơn Bão', 'Người dân một đảo nhỏ đoàn kết trước cơn bão lớn nhất trong lịch sử.', 129, -360, 7.9, 2, ARRAY['Drama','Adventure']),
    ('code_amber', 'Mật Mã Hổ Phách', 'Chuyên gia giải mã truy tìm kho lưu trữ biến mất từ thời chiến.', 136, -300, 8.0, 2, ARRAY['War','Mystery','Thriller']),
    ('home_team', 'Đội Nhà', 'Đội bóng thiếu niên học cách tin nhau trước trận chung kết toàn quốc.', 105, -240, 7.8, 2, ARRAY['Family','Comedy','Drama']),
    ('after_rain', 'Sau Cơn Mưa', 'Một đầu bếp trở lại quê hương và mở lại quán ăn của mẹ.', 113, -180, 8.1, 2, ARRAY['Drama','Romance']),
    ('archived_movie', 'Bản Phim Lưu Trữ', 'Bản ghi đã ngừng phát hành, dùng để kiểm thử trạng thái xóa mềm.', 90, -900, 6.5, -1, ARRAY['Documentary']);

INSERT INTO movie
    (title, description, duration_minutes, poster_url, trailer_url, release_date,
     rating, status, created_at, updated_at, created_by, updated_by)
SELECT
    i.title,
    i.description,
    i.duration_minutes,
    'https://placehold.co/600x900/111827/FFFFFF?text=' || replace(i.seed_key, '_', '+'),
    'https://www.youtube.com/results?search_query=' || replace(i.seed_key, '_', '+') || '+trailer',
    CURRENT_DATE + i.release_offset,
    i.rating,
    i.status,
    NOW(),
    NOW(),
    -8,
    -8
FROM seed_movie_input i;

CREATE TEMP TABLE seed_movie ON COMMIT DROP AS
SELECT i.seed_key, m.id, m.duration_minutes, m.status
FROM seed_movie_input i
JOIN movie m
  ON m.title = i.title
 AND m.created_by = -8;

INSERT INTO movie_genre (movie_id, genre_id)
SELECT DISTINCT m.id, g.id
FROM seed_movie_input i
JOIN seed_movie m ON m.seed_key = i.seed_key
CROSS JOIN LATERAL unnest(i.genre_names) AS requested_genre(name)
JOIN genre g ON g.name = requested_genre.name
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------
-- 2. Cinemas, auditoriums, and physical seats
-- ---------------------------------------------------------

CREATE TEMP TABLE seed_cinema_input (
    seed_key   VARCHAR(30) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    district   VARCHAR(100),
    address    VARCHAR(500) NOT NULL,
    phone      VARCHAR(20),
    status     SMALLINT NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_cinema_input (seed_key, name, city, district, address, phone, status)
VALUES
    ('hcm_central', 'Cinema Central Landmark', 'Hồ Chí Minh', 'Bình Thạnh', '208 Nguyễn Hữu Cảnh, Phường 22', '02873001001', 1),
    ('hcm_crescent', 'Cinema Crescent Mall', 'Hồ Chí Minh', 'Quận 7', '101 Tôn Dật Tiên, Tân Phú', '02873001002', 1),
    ('hanoi_lake', 'Cinema Hồ Gươm', 'Hà Nội', 'Hoàn Kiếm', '36 Tràng Tiền, Tràng Tiền', '02473001003', 1),
    ('danang_river', 'Cinema Sông Hàn', 'Đà Nẵng', 'Hải Châu', '128 Bạch Đằng, Hải Châu 1', '023673001004', 1),
    ('cantho_ninhkieu', 'Cinema Ninh Kiều', 'Cần Thơ', 'Ninh Kiều', '2 Hai Bà Trưng, Tân An', '029273001005', 1),
    ('nhatrang_bay', 'Cinema Vịnh Nha Trang', 'Khánh Hòa', 'Nha Trang', '20 Trần Phú, Lộc Thọ', '025873001006', 1),
    ('haiphong_harbor', 'Cinema Cảng Xanh', 'Hải Phòng', 'Hồng Bàng', '15 Minh Khai, Hoàng Văn Thụ', '022573001007', 0);

INSERT INTO cinema
    (name, city, district, address, phone, status, created_at, updated_at, created_by, updated_by)
SELECT name, city, district, address, phone, status, NOW(), NOW(), -8, -8
FROM seed_cinema_input;

CREATE TEMP TABLE seed_cinema ON COMMIT DROP AS
SELECT i.seed_key, c.id, c.status
FROM seed_cinema_input i
JOIN cinema c
  ON c.name = i.name
 AND c.created_by = -8;

CREATE TEMP TABLE seed_auditorium_input (
    cinema_key      VARCHAR(30) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    screen_type     SMALLINT NOT NULL,
    total_rows      INT NOT NULL,
    total_columns   INT NOT NULL,
    status          SMALLINT NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_auditorium_input
    (cinema_key, name, screen_type, total_rows, total_columns, status)
SELECT c.seed_key, a.name, a.screen_type, a.total_rows, a.total_columns,
       CASE WHEN c.seed_key = 'danang_river' AND a.name = 'Phòng 3' THEN 0 ELSE 1 END
FROM seed_cinema c
CROSS JOIN (VALUES
    ('Phòng 1', 0::SMALLINT, 8, 12),
    ('Phòng 2', 1::SMALLINT, 9, 14),
    ('Phòng 3', 2::SMALLINT, 10, 16)
) AS a(name, screen_type, total_rows, total_columns);

-- Give selected flagship rooms a 4DX screen while keeping all screen types represented.
UPDATE seed_auditorium_input
SET screen_type = 3
WHERE name = 'Phòng 3'
  AND cinema_key IN ('hcm_crescent', 'cantho_ninhkieu');

INSERT INTO auditorium
    (cinema_id, name, screen_type, total_rows, total_columns, status,
     created_at, updated_at, created_by, updated_by)
SELECT c.id, i.name, i.screen_type, i.total_rows, i.total_columns, i.status,
       NOW(), NOW(), -8, -8
FROM seed_auditorium_input i
JOIN seed_cinema c ON c.seed_key = i.cinema_key;

CREATE TEMP TABLE seed_auditorium ON COMMIT DROP AS
SELECT
    i.cinema_key,
    a.id,
    a.name,
    a.total_rows,
    a.total_columns,
    a.status,
    c.status AS cinema_status,
    row_number() OVER (ORDER BY a.id) AS auditorium_no
FROM seed_auditorium_input i
JOIN seed_cinema c ON c.seed_key = i.cinema_key
JOIN auditorium a
  ON a.cinema_id = c.id
 AND a.name = i.name
 AND a.created_by = -8;

-- The final row contains couple seats at odd-numbered starting positions; the
-- following even position is intentionally omitted because the couple seat
-- occupies both grid columns.
INSERT INTO seat (auditorium_id, seat_row, seat_number, seat_type, is_active)
SELECT
    a.id,
    chr(64 + row_no),
    column_no,
    CASE
        WHEN row_no = a.total_rows THEN 2
        WHEN row_no >= a.total_rows - 2 THEN 1
        ELSE 0
    END,
    NOT (
        row_no = 2
        AND column_no = 3
        AND mod(a.auditorium_no, 5) = 0
    )
FROM seed_auditorium a
CROSS JOIN LATERAL generate_series(1, a.total_rows) AS rows(row_no)
CROSS JOIN LATERAL generate_series(1, a.total_columns) AS columns(column_no)
WHERE row_no <> a.total_rows OR mod(column_no, 2) = 1;

-- ---------------------------------------------------------
-- 3. Users and safe token-history samples
-- ---------------------------------------------------------

CREATE TEMP TABLE seed_user_input (
    email     VARCHAR(100) PRIMARY KEY,
    role      SMALLINT NOT NULL,
    status    SMALLINT NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_user_input (email, role, status)
VALUES
    ('admin@cinema.local', 2, 0),
    ('manager.hcm@cinema.local', 1, 0),
    ('manager.hanoi@cinema.local', 1, 0),
    ('staff.support@cinema.local', 1, 0),
    ('locked.user@cinema.local', 0, 1),
    ('deleted.user@cinema.local', 0, -1);

INSERT INTO seed_user_input (email, role, status)
SELECT 'customer' || lpad(n::TEXT, 2, '0') || '@cinema.local', 0, 0
FROM generate_series(1, 30) AS numbers(n);

-- All active demo accounts use password: Cinema@123
INSERT INTO users
    (email, password_hash, role, status, created_at, updated_at, created_by, updated_by)
SELECT
    email,
    '$2a$10$H.yvwiZ5/aHiJTx94PXQj.eGAnnGBKqM0F.rH8IOmGIAghZMd/1Gq',
    role,
    status,
    NOW() - (row_number() OVER (ORDER BY email) || ' days')::INTERVAL,
    NOW(),
    -8,
    -8
FROM seed_user_input;

CREATE TEMP TABLE seed_user ON COMMIT DROP AS
SELECT
    i.email,
    u.id,
    i.role,
    i.status
FROM seed_user_input i
JOIN users u ON lower(u.email) = lower(i.email);

-- Token samples are intentionally expired and revoked. No usable raw token is
-- distributed with the project.
INSERT INTO refresh_token (user_id, token_hash, expires_at, created_at, is_revoked)
SELECT
    id,
    md5('revoked-sample-token-' || id) || md5('expired-sample-token-' || id),
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '9 days',
    1
FROM seed_user
WHERE role = 0 AND status = 0
ORDER BY id
LIMIT 12;

-- ---------------------------------------------------------
-- 4. Showtimes and per-showtime seat snapshots
-- ---------------------------------------------------------

CREATE TEMP TABLE seed_active_auditorium ON COMMIT DROP AS
SELECT
    a.*,
    row_number() OVER (ORDER BY a.id) AS active_no
FROM seed_auditorium a
WHERE a.status = 1
  AND a.cinema_status = 1;

CREATE TEMP TABLE seed_now_movie ON COMMIT DROP AS
SELECT
    m.*,
    row_number() OVER (ORDER BY m.id) AS movie_no
FROM seed_movie m
WHERE m.status = 1;

CREATE TEMP TABLE seed_ended_movie ON COMMIT DROP AS
SELECT
    m.*,
    row_number() OVER (ORDER BY m.id) AS movie_no
FROM seed_movie m
WHERE m.status = 2;

-- Seven upcoming days, four non-overlapping OPEN showtimes per active room.
INSERT INTO showtime
    (movie_id, auditorium_id, start_time, end_time, base_price, status,
     created_at, updated_at, created_by, updated_by)
SELECT
    m.id,
    a.id,
    ((CURRENT_DATE + day_offset) + slot_time) AT TIME ZONE 'Asia/Ho_Chi_Minh',
    (((CURRENT_DATE + day_offset) + slot_time) AT TIME ZONE 'Asia/Ho_Chi_Minh')
        + make_interval(mins => m.duration_minutes),
    75000 + (slot_no - 1) * 10000 + CASE WHEN a.name = 'Phòng 3' THEN 20000 ELSE 0 END,
    1,
    NOW(),
    NOW(),
    -8,
    -8
FROM seed_active_auditorium a
CROSS JOIN generate_series(1, 7) AS days(day_offset)
CROSS JOIN (VALUES
    (1, TIME '09:00'),
    (2, TIME '13:00'),
    (3, TIME '17:00'),
    (4, TIME '21:00')
) AS slots(slot_no, slot_time)
CROSS JOIN (SELECT count(*) AS total FROM seed_now_movie) movie_count
JOIN seed_now_movie m
  ON m.movie_no = mod(a.active_no + day_offset + slot_no - 3, movie_count.total) + 1;

-- Four historical days, three COMPLETED showtimes per active room.
INSERT INTO showtime
    (movie_id, auditorium_id, start_time, end_time, base_price, status,
     created_at, updated_at, created_by, updated_by)
SELECT
    m.id,
    a.id,
    ((CURRENT_DATE - day_offset) + slot_time) AT TIME ZONE 'Asia/Ho_Chi_Minh',
    (((CURRENT_DATE - day_offset) + slot_time) AT TIME ZONE 'Asia/Ho_Chi_Minh')
        + make_interval(mins => m.duration_minutes),
    65000 + (slot_no - 1) * 10000 + CASE WHEN a.name = 'Phòng 3' THEN 20000 ELSE 0 END,
    2,
    NOW() - make_interval(days => day_offset + 7),
    NOW() - make_interval(days => day_offset),
    -8,
    -8
FROM seed_active_auditorium a
CROSS JOIN generate_series(1, 4) AS days(day_offset)
CROSS JOIN (VALUES
    (1, TIME '10:00'),
    (2, TIME '15:00'),
    (3, TIME '20:00')
) AS slots(slot_no, slot_time)
CROSS JOIN (SELECT count(*) AS total FROM seed_ended_movie) movie_count
JOIN seed_ended_movie m
  ON m.movie_no = mod(a.active_no + day_offset + slot_no - 3, movie_count.total) + 1;

-- One DRAFT and one CANCELLED record per active room provide admin/state samples.
INSERT INTO showtime
    (movie_id, auditorium_id, start_time, end_time, base_price, status,
     created_at, updated_at, created_by, updated_by)
SELECT
    m.id,
    a.id,
    ((CURRENT_DATE + 9) + TIME '10:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
    (((CURRENT_DATE + 9) + TIME '10:00') AT TIME ZONE 'Asia/Ho_Chi_Minh')
        + make_interval(mins => m.duration_minutes),
    80000,
    0,
    NOW(),
    NOW(),
    -8,
    -8
FROM seed_active_auditorium a
CROSS JOIN (SELECT count(*) AS total FROM seed_now_movie) movie_count
JOIN seed_now_movie m ON m.movie_no = mod(a.active_no - 1, movie_count.total) + 1;

INSERT INTO showtime
    (movie_id, auditorium_id, start_time, end_time, base_price, status,
     created_at, updated_at, created_by, updated_by)
SELECT
    m.id,
    a.id,
    ((CURRENT_DATE + 9) + TIME '15:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
    (((CURRENT_DATE + 9) + TIME '15:00') AT TIME ZONE 'Asia/Ho_Chi_Minh')
        + make_interval(mins => m.duration_minutes),
    90000,
    -1,
    NOW(),
    NOW(),
    -8,
    -8
FROM seed_active_auditorium a
CROSS JOIN (SELECT count(*) AS total FROM seed_now_movie) movie_count
JOIN seed_now_movie m ON m.movie_no = mod(a.active_no + 3, movie_count.total) + 1;

CREATE TEMP TABLE seed_showtime ON COMMIT DROP AS
SELECT id, auditorium_id, start_time, status, base_price
FROM showtime
WHERE created_by = -8;

-- Snapshot every active physical seat for every generated showtime. Pricing
-- follows SeatType: STANDARD x1.0, VIP x1.5, COUPLE x2.0.
INSERT INTO showtime_seat (showtime_id, seat_id, status, price, held_at, version)
SELECT
    st.id,
    s.id,
    0,
    round(st.base_price * CASE s.seat_type WHEN 1 THEN 1.5 WHEN 2 THEN 2.0 ELSE 1.0 END),
    NULL,
    0
FROM seed_showtime st
JOIN seat s ON s.auditorium_id = st.auditorium_id
WHERE s.is_active = TRUE;

-- ---------------------------------------------------------
-- 5. Bookings, booking-seat snapshots, and payments
-- ---------------------------------------------------------

CREATE TEMP TABLE seed_customer ON COMMIT DROP AS
SELECT
    id,
    row_number() OVER (ORDER BY id) AS customer_no
FROM seed_user
WHERE role = 0 AND status = 0;

CREATE TEMP TABLE seed_booking_plan ON COMMIT DROP AS
WITH completed_showtimes AS (
    SELECT id, start_time, status
    FROM seed_showtime
    WHERE status = 2
    ORDER BY start_time, id
    LIMIT 60
),
open_showtimes AS (
    SELECT id, start_time, status
    FROM seed_showtime
    WHERE status = 1
    ORDER BY start_time, id
    LIMIT 100
),
selected_showtimes AS (
    SELECT * FROM completed_showtimes
    UNION ALL
    SELECT * FROM open_showtimes
),
expanded AS (
    SELECT
        st.id AS showtime_id,
        st.start_time,
        st.status AS showtime_status,
        booking_group,
        row_number() OVER (ORDER BY st.start_time, st.id, booking_group) AS plan_no,
        row_number() OVER (PARTITION BY st.status ORDER BY st.start_time, st.id, booking_group) AS status_plan_no
    FROM selected_showtimes st
    CROSS JOIN generate_series(1, 4) AS groups(booking_group)
),
customer_count AS (
    SELECT count(*) AS total FROM seed_customer
)
SELECT
    e.plan_no,
    'SMP-' || lpad(e.plan_no::TEXT, 10, '0') AS booking_code,
    e.showtime_id,
    e.start_time,
    e.showtime_status,
    e.booking_group,
    c.id AS customer_id,
    CASE
        WHEN e.showtime_status = 2 AND e.booking_group <= 2 THEN 1
        WHEN e.showtime_status = 2 AND e.booking_group = 3 THEN -1
        WHEN e.showtime_status = 2 THEN 2
        WHEN e.booking_group <= 2 THEN 1
        WHEN e.booking_group = 3 AND e.status_plan_no <= 100 THEN 0
        WHEN e.booking_group = 3 THEN 2
        ELSE -1
    END::SMALLINT AS booking_status
FROM expanded e
CROSS JOIN customer_count cc
JOIN seed_customer c ON c.customer_no = mod(e.plan_no - 1, cc.total) + 1;

-- Distribute seat choices throughout each room instead of always occupying the
-- first rows. Every booking gets two unique seats.
CREATE TEMP TABLE seed_booking_seat_plan ON COMMIT DROP AS
WITH ranked_seats AS (
    SELECT
        ss.showtime_id,
        ss.id AS showtime_seat_id,
        s.seat_row,
        s.seat_number,
        s.seat_type,
        ss.price,
        row_number() OVER (
            PARTITION BY ss.showtime_id
            ORDER BY mod(ss.id * 37, 997), ss.id
        ) AS seat_no
    FROM showtime_seat ss
    JOIN seat s ON s.id = ss.seat_id
    JOIN seed_showtime st ON st.id = ss.showtime_id
)
SELECT
    bp.booking_code,
    rs.showtime_seat_id,
    rs.seat_row || rs.seat_number AS seat_label,
    rs.seat_type,
    rs.price
FROM seed_booking_plan bp
JOIN ranked_seats rs
  ON rs.showtime_id = bp.showtime_id
 AND rs.seat_no BETWEEN ((bp.booking_group - 1) * 2 + 1) AND (bp.booking_group * 2);

INSERT INTO booking
    (booking_code, customer_id, showtime_id, status, total_amount, expires_at,
     idempotency_key, request_hash, created_at, updated_at, created_by, updated_by)
SELECT
    bp.booking_code,
    bp.customer_id,
    bp.showtime_id,
    bp.booking_status,
    sum(sp.price),
    CASE
        WHEN bp.booking_status = 0 THEN NOW() + INTERVAL '2 hours'
        WHEN bp.showtime_status = 2 THEN bp.start_time - INTERVAL '2 days' + INTERVAL '15 minutes'
        ELSE NOW() - INTERVAL '1 day'
    END,
    ('00000000-0000-4000-8000-' || lpad(bp.plan_no::TEXT, 12, '0'))::UUID,
    md5(bp.booking_code || ':' || bp.showtime_id)
        || md5(bp.showtime_id || ':' || bp.booking_code),
    CASE
        WHEN bp.showtime_status = 2 THEN bp.start_time - INTERVAL '2 days'
        ELSE NOW() - make_interval(hours => (mod(bp.plan_no, 72) + 1)::INT)
    END,
    NOW(),
    -8,
    -8
FROM seed_booking_plan bp
JOIN seed_booking_seat_plan sp ON sp.booking_code = bp.booking_code
GROUP BY
    bp.plan_no, bp.booking_code, bp.customer_id, bp.showtime_id,
    bp.booking_status, bp.showtime_status, bp.start_time;

CREATE TEMP TABLE seed_booking ON COMMIT DROP AS
SELECT b.id, b.booking_code, b.status, b.total_amount, b.created_at, bp.plan_no
FROM booking b
JOIN seed_booking_plan bp ON bp.booking_code = b.booking_code
WHERE b.created_by = -8;

INSERT INTO booking_seat
    (booking_id, showtime_seat_id, seat_label, seat_type, price)
SELECT b.id, sp.showtime_seat_id, sp.seat_label, sp.seat_type, sp.price
FROM seed_booking_seat_plan sp
JOIN seed_booking b ON b.booking_code = sp.booking_code;

-- Synchronize seat lifecycle with the active booking snapshots.
UPDATE showtime_seat ss
SET status = 2,
    held_at = NULL,
    version = version + 1
FROM booking_seat bs
JOIN seed_booking b ON b.id = bs.booking_id
WHERE ss.id = bs.showtime_seat_id
  AND b.status = 1;

UPDATE showtime_seat ss
SET status = 1,
    held_at = NOW(),
    version = version + 1
FROM booking_seat bs
JOIN seed_booking b ON b.id = bs.booking_id
WHERE ss.id = bs.showtime_seat_id
  AND b.status = 0;

-- Every booking receives a representative payment attempt. Successful
-- payments correspond to CONFIRMED bookings; PENDING bookings remain pending;
-- cancelled/expired bookings retain a failed attempt for history screens.
INSERT INTO payment
    (booking_id, payment_reference, provider, status, amount,
     provider_transaction_no, response_code, bank_code, failure_reason, paid_at,
     created_at, updated_at, created_by, updated_by)
SELECT
    b.id,
    'SAMPLE-PAY-' || lpad(b.plan_no::TEXT, 10, '0'),
    'VNPAY',
    CASE WHEN b.status = 1 THEN 1 WHEN b.status = 0 THEN 0 ELSE -1 END,
    b.total_amount,
    CASE WHEN b.status = 1 THEN 'VNP' || lpad(b.plan_no::TEXT, 12, '0') ELSE NULL END,
    CASE WHEN b.status = 1 THEN '00' WHEN b.status = 0 THEN NULL ELSE '24' END,
    CASE mod(b.plan_no, 4) WHEN 0 THEN 'NCB' WHEN 1 THEN 'VCB' WHEN 2 THEN 'TCB' ELSE 'MB' END,
    CASE WHEN b.status IN (-1, 2) THEN 'Giao dịch mẫu không hoàn tất' ELSE NULL END,
    CASE WHEN b.status = 1 THEN b.created_at + INTERVAL '3 minutes' ELSE NULL END,
    b.created_at + INTERVAL '1 minute',
    b.created_at + INTERVAL '3 minutes',
    -8,
    -8
FROM seed_booking b;

-- Some successful bookings include an earlier failed retry, demonstrating the
-- one-booking-to-many-payment-attempts relationship.
INSERT INTO payment
    (booking_id, payment_reference, provider, status, amount,
     provider_transaction_no, response_code, bank_code, failure_reason, paid_at,
     created_at, updated_at, created_by, updated_by)
SELECT
    b.id,
    'SAMPLE-RETRY-' || lpad(b.plan_no::TEXT, 10, '0'),
    'VNPAY',
    -1,
    b.total_amount,
    NULL,
    '24',
    'VCB',
    'Khách hàng hủy lần thanh toán đầu tiên',
    NULL,
    b.created_at + INTERVAL '30 seconds',
    b.created_at + INTERVAL '30 seconds',
    -8,
    -8
FROM seed_booking b
WHERE b.status = 1
  AND mod(b.plan_no, 5) = 0;

-- Keep sequence values correct even if this migration follows databases that
-- were partially populated by hand.
SELECT setval(pg_get_serial_sequence('genre', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM genre;
SELECT setval(pg_get_serial_sequence('movie', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM movie;
SELECT setval(pg_get_serial_sequence('cinema', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM cinema;
SELECT setval(pg_get_serial_sequence('auditorium', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM auditorium;
SELECT setval(pg_get_serial_sequence('seat', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM seat;
SELECT setval(pg_get_serial_sequence('showtime', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM showtime;
SELECT setval(pg_get_serial_sequence('showtime_seat', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM showtime_seat;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM users;
SELECT setval(pg_get_serial_sequence('refresh_token', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM refresh_token;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM booking;
SELECT setval(pg_get_serial_sequence('booking_seat', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM booking_seat;
SELECT setval(pg_get_serial_sequence('payment', 'id'), COALESCE(max(id), 1), max(id) IS NOT NULL) FROM payment;
