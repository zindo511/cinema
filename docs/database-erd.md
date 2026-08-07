# ERD database từ Flyway migrations

Tài liệu này được dựng từ các migration `V1` đến `V9` trong
`src/main/resources/db/migration`. Migration `V8` chỉ seed dữ liệu nên không tạo
thêm bảng nghiệp vụ.

## Quy ước

- `PK`: primary key; `FK`: foreign key; `UK`: unique key/index.
- `||`: đúng một; `o|`: không hoặc một; `o{`: không hoặc nhiều.
- Đường liền biểu diễn quan hệ bắt buộc ở phía giữ FK; đường chấm dùng cho FK
  nullable (`ticket.scanned_by`).
- Các cột `created_by` và `updated_by` là `BIGINT`, nhưng Flyway không khai báo
  FK tới `users`, vì vậy sơ đồ không nối các cột audit này với bảng `users`.
- Các sơ đồ nhỏ là nhóm aggregate/domain theo package và trách nhiệm nghiệp vụ
  hiện tại. Entity có hậu tố `(external)` thuộc nhóm khác và chỉ được đưa vào để
  thấy dependency qua FK.

## 1. Movie Catalog

Aggregate chính: `movie`; `genre` là master-data dùng chung; `movie_genre` là
bảng liên kết N-N.

```mermaid
erDiagram
    direction LR

    movie ||--o{ movieGenre : has
    genre ||--o{ movieGenre : classifies

    movie["movie"] {
        bigint id PK
        varchar title
        text description
        int duration_minutes
        varchar poster_url
        varchar trailer_url
        date release_date
        decimal rating "DECIMAL(2,1)"
        smallint status "0 COMING_SOON, 1 NOW_SHOWING, 2 ENDED, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    genre["genre"] {
        bigint id PK
        varchar name UK
    }

    movieGenre["movie_genre"] {
        bigint movie_id PK, FK
        bigint genre_id PK, FK
    }
```

## 2. Cinema Layout

Aggregate/domain quản lý cấu trúc vật lý của rạp: `cinema` → `auditorium` →
`seat`.

```mermaid
erDiagram
    direction LR

    cinema ||--o{ auditorium : contains
    auditorium ||--o{ seat : contains

    cinema["cinema"] {
        bigint id PK
        varchar name
        varchar city
        varchar district
        varchar address
        varchar phone
        smallint status "1 ACTIVE, 0 INACTIVE, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    auditorium["auditorium"] {
        bigint id PK
        bigint cinema_id FK "composite UK with name"
        varchar name "composite UK with cinema_id"
        smallint screen_type "0 2D, 1 3D, 2 IMAX, 3 4DX"
        int total_rows
        int total_columns
        smallint status "1 ACTIVE, 0 MAINTENANCE, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    seat["seat"] {
        bigint id PK
        bigint auditorium_id FK "composite UK with row and number"
        varchar seat_row "composite UK"
        int seat_number "composite UK"
        smallint seat_type "0 STANDARD, 1 VIP, 2 COUPLE"
        boolean is_active
    }
```

## 3. Showtime Inventory

Aggregate chính: `showtime`; `showtime_seat` là snapshot tồn kho và giá ghế cho
từng suất chiếu.

```mermaid
erDiagram
    direction LR

    movieRef ||--o{ showtime : scheduled_as
    auditoriumRef ||--o{ showtime : hosts
    showtime ||--o{ showtimeSeat : snapshots
    seatRef ||--o{ showtimeSeat : copied_to

    movieRef["movie (external)"] {
        bigint id PK
    }

    auditoriumRef["auditorium (external)"] {
        bigint id PK
    }

    seatRef["seat (external)"] {
        bigint id PK
    }

    showtime["showtime"] {
        bigint id PK
        bigint movie_id FK
        bigint auditorium_id FK
        timestamptz start_time
        timestamptz end_time
        decimal base_price "DECIMAL(10,0)"
        smallint status "0 DRAFT, 1 OPEN, 2 COMPLETED, -1 CANCELLED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    showtimeSeat["showtime_seat"] {
        bigint id PK
        bigint showtime_id FK "composite UK with seat_id"
        bigint seat_id FK "composite UK with showtime_id"
        smallint status "0 AVAILABLE, 1 HELD, 2 BOOKED"
        decimal price "DECIMAL(10,0)"
        timestamptz held_at
        int version "optimistic lock"
    }
```

## 4. Identity

Aggregate chính: `users`; `refresh_token` lưu lịch sử token đã băm phục vụ JWT
rotation.

```mermaid
erDiagram
    direction LR

    users ||--o{ refreshToken : owns

    users["users"] {
        bigint id PK
        varchar email UK "unique on lower(email)"
        varchar password_hash
        smallint role "0 USER, 1 STAFF, 2 ADMIN"
        smallint status "0 ACTIVE, 1 LOCKED, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    refreshToken["refresh_token"] {
        bigint id PK
        bigint user_id FK
        varchar token_hash UK
        timestamptz expires_at
        timestamptz created_at
        smallint is_revoked "0 ACTIVE, 1 REVOKED"
    }
```

## 5. Booking và Ticket

Aggregate `booking` sở hữu các snapshot `booking_seat`. `ticket` nằm trong
booking domain hiện tại và có quan hệ 0-1 với mỗi `booking_seat`.

```mermaid
erDiagram
    direction LR

    userRef ||--o{ booking : places
    showtimeRef ||--o{ booking : receives
    booking ||--o{ bookingSeat : contains
    showtimeSeatRef ||--o{ bookingSeat : selected_as
    bookingSeat ||--o| ticket : issues
    userRef o|..o{ ticket : scans

    userRef["users (external)"] {
        bigint id PK
    }

    showtimeRef["showtime (external)"] {
        bigint id PK
    }

    showtimeSeatRef["showtime_seat (external)"] {
        bigint id PK
    }

    booking["booking"] {
        bigint id PK
        varchar booking_code UK
        bigint customer_id FK "composite UK with idempotency_key"
        bigint showtime_id FK
        smallint status "-1 CANCELLED, 0 PENDING, 1 CONFIRMED, 2 EXPIRED"
        decimal total_amount "DECIMAL(12,0)"
        timestamptz expires_at
        uuid idempotency_key "composite UK with customer_id"
        varchar request_hash
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    bookingSeat["booking_seat"] {
        bigint id PK
        bigint booking_id FK "composite UK with showtime_seat_id"
        bigint showtime_seat_id FK "composite UK with booking_id"
        varchar seat_label
        smallint seat_type "0 STANDARD, 1 VIP, 2 COUPLE"
        decimal price "DECIMAL(10,0)"
    }

    ticket["ticket"] {
        bigint id PK
        varchar ticket_code UK
        bigint booking_seat_id FK, UK
        smallint status
        timestamptz scanned_at
        bigint scanned_by FK "nullable"
        timestamptz created_at
        timestamptz updated_at
    }
```

## 6. Payment

Mỗi hàng `payment` là một payment attempt độc lập; một booking có thể có nhiều
lần thử thanh toán.

```mermaid
erDiagram
    direction LR

    bookingRef ||--o{ payment : has_attempts

    bookingRef["booking (external)"] {
        bigint id PK
    }

    payment["payment"] {
        bigint id PK
        bigint booking_id FK
        varchar payment_reference UK
        varchar provider
        smallint status "-1 FAILED, 0 PENDING, 1 SUCCESS"
        decimal amount "DECIMAL(12,0)"
        varchar provider_transaction_no
        varchar response_code
        varchar bank_code
        varchar failure_reason
        timestamptz paid_at
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }
```

## 7. Toàn bộ database hiện có

Sơ đồ này chứa đủ 14 bảng nghiệp vụ được tạo bởi Flyway. Bảng metadata nội bộ
`flyway_schema_history` không được đưa vào vì không thuộc schema nghiệp vụ của
ứng dụng.

```mermaid
erDiagram
    direction TB

    movie ||--o{ movieGenre : has
    genre ||--o{ movieGenre : classifies

    cinema ||--o{ auditorium : contains
    auditorium ||--o{ seat : contains

    movie ||--o{ showtime : scheduled_as
    auditorium ||--o{ showtime : hosts
    showtime ||--o{ showtimeSeat : snapshots
    seat ||--o{ showtimeSeat : copied_to

    users ||--o{ refreshToken : owns
    users ||--o{ booking : places
    showtime ||--o{ booking : receives
    booking ||--o{ bookingSeat : contains
    showtimeSeat ||--o{ bookingSeat : selected_as

    booking ||--o{ payment : has_attempts
    bookingSeat ||--o| ticket : issues
    users o|..o{ ticket : scans

    movie["movie"] {
        bigint id PK
        varchar title
        text description
        int duration_minutes
        varchar poster_url
        varchar trailer_url
        date release_date
        decimal rating "DECIMAL(2,1)"
        smallint status "0 COMING_SOON, 1 NOW_SHOWING, 2 ENDED, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    genre["genre"] {
        bigint id PK
        varchar name UK
    }

    movieGenre["movie_genre"] {
        bigint movie_id PK, FK
        bigint genre_id PK, FK
    }

    cinema["cinema"] {
        bigint id PK
        varchar name
        varchar city
        varchar district
        varchar address
        varchar phone
        smallint status "1 ACTIVE, 0 INACTIVE, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    auditorium["auditorium"] {
        bigint id PK
        bigint cinema_id FK "composite UK with name"
        varchar name "composite UK with cinema_id"
        smallint screen_type "0 2D, 1 3D, 2 IMAX, 3 4DX"
        int total_rows
        int total_columns
        smallint status "1 ACTIVE, 0 MAINTENANCE, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    seat["seat"] {
        bigint id PK
        bigint auditorium_id FK "composite UK with row and number"
        varchar seat_row "composite UK"
        int seat_number "composite UK"
        smallint seat_type "0 STANDARD, 1 VIP, 2 COUPLE"
        boolean is_active
    }

    showtime["showtime"] {
        bigint id PK
        bigint movie_id FK
        bigint auditorium_id FK
        timestamptz start_time
        timestamptz end_time
        decimal base_price "DECIMAL(10,0)"
        smallint status "0 DRAFT, 1 OPEN, 2 COMPLETED, -1 CANCELLED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    showtimeSeat["showtime_seat"] {
        bigint id PK
        bigint showtime_id FK "composite UK with seat_id"
        bigint seat_id FK "composite UK with showtime_id"
        smallint status "0 AVAILABLE, 1 HELD, 2 BOOKED"
        decimal price "DECIMAL(10,0)"
        timestamptz held_at
        int version "optimistic lock"
    }

    users["users"] {
        bigint id PK
        varchar email UK "unique on lower(email)"
        varchar password_hash
        smallint role "0 USER, 1 STAFF, 2 ADMIN"
        smallint status "0 ACTIVE, 1 LOCKED, -1 DELETED"
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    refreshToken["refresh_token"] {
        bigint id PK
        bigint user_id FK
        varchar token_hash UK
        timestamptz expires_at
        timestamptz created_at
        smallint is_revoked "0 ACTIVE, 1 REVOKED"
    }

    booking["booking"] {
        bigint id PK
        varchar booking_code UK
        bigint customer_id FK "composite UK with idempotency_key"
        bigint showtime_id FK
        smallint status "-1 CANCELLED, 0 PENDING, 1 CONFIRMED, 2 EXPIRED"
        decimal total_amount "DECIMAL(12,0)"
        timestamptz expires_at
        uuid idempotency_key "composite UK with customer_id"
        varchar request_hash
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    bookingSeat["booking_seat"] {
        bigint id PK
        bigint booking_id FK "composite UK with showtime_seat_id"
        bigint showtime_seat_id FK "composite UK with booking_id"
        varchar seat_label
        smallint seat_type "0 STANDARD, 1 VIP, 2 COUPLE"
        decimal price "DECIMAL(10,0)"
    }

    payment["payment"] {
        bigint id PK
        bigint booking_id FK
        varchar payment_reference UK
        varchar provider
        smallint status "-1 FAILED, 0 PENDING, 1 SUCCESS"
        decimal amount "DECIMAL(12,0)"
        varchar provider_transaction_no
        varchar response_code
        varchar bank_code
        varchar failure_reason
        timestamptz paid_at
        timestamptz created_at
        timestamptz updated_at
        bigint created_by
        bigint updated_by
    }

    ticket["ticket"] {
        bigint id PK
        varchar ticket_code UK
        bigint booking_seat_id FK, UK
        smallint status
        timestamptz scanned_at
        bigint scanned_by FK "nullable"
        timestamptz created_at
        timestamptz updated_at
    }
```

## Danh sách FK hiện có

| Bảng con | Cột FK | Bảng cha | Ghi chú |
|---|---|---|---|
| `movie_genre` | `movie_id` | `movie.id` | `ON DELETE CASCADE` |
| `movie_genre` | `genre_id` | `genre.id` | `ON DELETE CASCADE` |
| `auditorium` | `cinema_id` | `cinema.id` |  |
| `seat` | `auditorium_id` | `auditorium.id` |  |
| `showtime` | `movie_id` | `movie.id` |  |
| `showtime` | `auditorium_id` | `auditorium.id` |  |
| `showtime_seat` | `showtime_id` | `showtime.id` |  |
| `showtime_seat` | `seat_id` | `seat.id` |  |
| `booking` | `customer_id` | `users.id` | Thêm ở `V5` |
| `booking` | `showtime_id` | `showtime.id` |  |
| `booking_seat` | `booking_id` | `booking.id` |  |
| `booking_seat` | `showtime_seat_id` | `showtime_seat.id` |  |
| `refresh_token` | `user_id` | `users.id` |  |
| `payment` | `booking_id` | `booking.id` |  |
| `ticket` | `booking_seat_id` | `booking_seat.id` | `UNIQUE`, tạo quan hệ 0-1 |
| `ticket` | `scanned_by` | `users.id` | Nullable |
