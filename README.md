# 🎬 CineGo — Cinema Booking System

Hệ thống đặt vé xem phim trực tuyến xây dựng trên **Spring Boot**. Hỗ trợ toàn bộ luồng nghiệp vụ từ duyệt phim, chọn suất chiếu, chọn ghế, thanh toán, xuất vé đến check-in bằng QR.

---

## 📑 Mục lục

- [Tổng quan](#tổng-quan)
- [Luồng nghiệp vụ](#luồng-nghiệp-vụ)
- [Tech Stack](#tech-stack)
- [Kiến trúc dự án](#kiến-trúc-dự-án)
- [Cài đặt và chạy](#cài-đặt-và-chạy)
- [Biến môi trường](#biến-môi-trường)
- [Database](#database)
- [API Overview](#api-overview)
- [Concurrency & Bảo toàn dữ liệu](#concurrency--bảo-toàn-dữ-liệu)
- [Testing](#testing)
- [Tài khoản demo](#tài-khoản-demo)
- [Tài liệu tham khảo](#tài-liệu-tham-khảo)

---

## Tổng quan

CineGo là hệ thống đặt vé xem phim với các tính năng chính:

- **Quản lý phim** — CRUD phim, thể loại, trạng thái (Sắp chiếu / Đang chiếu / Đã kết thúc).
- **Quản lý rạp** — Hệ thống rạp, phòng chiếu (2D, 3D, IMAX, 4DX) và sơ đồ ghế (Standard, VIP, Couple).
- **Suất chiếu** — Tạo, duyệt, hủy suất chiếu với snapshot ghế và giá tại thời điểm tạo.
- **Đặt vé** — Chọn ghế real-time, giữ ghế tạm thời (HELD) 10 phút, chống double booking bằng pessimistic lock.
- **Thanh toán** — Tích hợp VNPay sandbox, xử lý callback IPN và hoàn tiền.
- **Vé & QR Check-in** — Xuất vé điện tử với mã QR, nhân viên quét check-in.
- **Xác thực & phân quyền** — JWT + Refresh Token, role-based access (User / Staff / Admin).
- **Idempotency** — Chống tạo booking trùng khi client retry bằng `Idempotency-Key` header.

---

## Luồng nghiệp vụ

```text
Movie → Cinema → Showtime → Seat Selection → Booking → Payment → Ticket → QR Check-in
```

### Trạng thái booking

```text
[new] → PENDING → CONFIRMED → CANCELLED
                → EXPIRED
```

### Trạng thái ghế theo suất chiếu

```text
AVAILABLE → HELD → BOOKED → AVAILABLE (khi hủy vé hoặc booking hết hạn)
```

---

## Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4.1 |
| Security | Spring Security + OAuth2 Resource Server (JWT) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Migration | Flyway |
| Mapping | MapStruct 1.6.3 |
| Boilerplate | Lombok |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Payment | VNPay Sandbox |

---

## Kiến trúc dự án

```text
backend/
└── src/main/java/vn/cinema/
    ├── api/                        # REST Controllers
    │   ├── AuthController
    │   ├── MovieController
    │   ├── CinemaController
    │   ├── AuditoriumController
    │   ├── ShowtimeController
    │   ├── BookingController
    │   ├── PaymentController
    │   ├── TicketController
    │   └── GlobalExceptionHandler
    ├── app/                        # Application layer (DTOs, Services, Mappers)
    ├── config/                     # Security, CORS, Scheduler config
    ├── domain/                     # Domain layer
    │   ├── booking/                # Booking, BookingSeat, BookingStatus
    │   ├── cinema/                 # Cinema, Auditorium, Seat
    │   ├── movie/                  # Movie, Genre
    │   ├── showtime/               # Showtime, ShowtimeSeat
    │   ├── payment/                # Payment
    │   ├── user/                   # User, RefreshToken
    │   └── common/                 # BaseEntity, Exceptions, Enums
    └── infrastructure/             # Schedulers, External adapters
```

---

## Cài đặt và chạy

### Yêu cầu

- **Java** 21+
- **Maven** 3.9+ (hoặc dùng Maven Wrapper `./mvnw`)
- **PostgreSQL** 15+

### 1. Database

Tạo database PostgreSQL:

```sql
CREATE DATABASE cinema_db;
```

Flyway sẽ tự động chạy migration khi backend khởi động (V1–V10), bao gồm cả dữ liệu mẫu (V8).

### 2. Chạy ứng dụng

```bash
cd backend

# Thiết lập biến môi trường (xem mục Biến môi trường)
# Chạy ứng dụng
mvn spring-boot:run

# Hoặc chạy test
mvn test
```

Backend chạy tại `http://localhost:8080`. Swagger UI có tại `http://localhost:8080/swagger-ui.html`.

---

## Biến môi trường

`application.yaml` đọc các biến sau từ environment:

| Biến | Mô tả | Ví dụ |
|---|---|---|
| `DB_USERNAME` | Username PostgreSQL | `postgres` |
| `DB_PASSWORD` | Password PostgreSQL | `secret` |
| `JWT_SECRET` | Secret key cho JWT signing | (chuỗi base64 đủ dài) |
| `YOUR_TMN_CODE` | VNPay Terminal Code | (từ VNPay sandbox) |
| `YOUR_HASH_SECRET` | VNPay Hash Secret | (từ VNPay sandbox) |

---

## Database

### Schema overview

Database gồm **14 bảng nghiệp vụ**, quản lý bởi **10 Flyway migrations** (V1–V10):

| Migration | Nội dung |
|---|---|
| V1 | `movie`, `genre`, `movie_genre` |
| V2 | `cinema`, `auditorium`, `seat` |
| V3 | `showtime`, `showtime_seat` |
| V4 | `booking`, `booking_seat` |
| V5 | `users` (+ FK `booking.customer_id`) |
| V6 | `refresh_token` |
| V7 | `payment` |
| V8 | Seed dữ liệu mẫu (~30 phim, 7 rạp, 700+ suất chiếu, 640+ booking) |
| V9 | `ticket` |
| V10 | `refund` |

### ERD (tóm tắt)

```text
movie ──┬── movie_genre ──── genre
        └── showtime ──┬── showtime_seat ──── seat
             │         └── booking_seat ──┬── ticket
             │                            │
cinema ── auditorium ──┘        booking ──┘──── payment
                                  │
                                users ──── refresh_token
```

> Chi tiết đầy đủ ERD với Mermaid diagrams: xem [`docs/database-erd.md`](docs/database-erd.md)

---

## API Overview

Base URL: `/api/v1`

### Authentication

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/auth/login` | Đăng nhập, nhận JWT + Refresh Token |
| POST | `/auth/register` | Đăng ký tài khoản |
| POST | `/auth/refresh` | Làm mới JWT bằng Refresh Token |

### Movies

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/movies` | Danh sách phim |
| GET | `/movies/{id}` | Chi tiết phim |

### Cinemas & Auditoriums

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/cinemas` | Danh sách rạp |
| GET | `/cinemas/{id}` | Chi tiết rạp |
| GET | `/cinemas/{id}/auditoriums` | Phòng chiếu của rạp |

### Showtimes

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/showtimes` | Lọc suất chiếu theo phim/rạp/ngày |
| GET | `/showtimes/{id}/seats` | Seat map (sơ đồ ghế + trạng thái) |

### Bookings

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/bookings` | Tạo booking (kèm `Idempotency-Key` header) |
| GET | `/bookings/history` | Lịch sử booking của user |

### Payments

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/payments/vnpay/create` | Tạo URL thanh toán VNPay |
| GET | `/payments/vnpay/ipn` | VNPay IPN callback |

### Tickets

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/tickets` | Lấy vé theo booking |
| POST | `/tickets/{code}/checkin` | Check-in vé bằng mã QR |

> Xem đầy đủ API contract tại Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Concurrency & Bảo toàn dữ liệu

Hệ thống đảm bảo **một ghế của một suất chiếu không thể thuộc về hai booking đang hoạt động** bằng chiến lược đa tầng:

1. **Pessimistic Locking** — `SELECT ... FOR UPDATE` khóa dòng `showtime` trước, sau đó khóa các `showtime_seat` theo thứ tự ID tăng dần.
2. **Lock Ordering** — Tất cả transaction ghi (booking, hủy suất chiếu, expiry) tuân theo cùng thứ tự khóa để tránh deadlock.
3. **Optimistic Locking** — `@Version` trên `ShowtimeSeat` làm lớp bảo vệ thứ hai.
4. **Idempotency** — Header `Idempotency-Key` (UUID) + unique constraint `(customer_id, idempotency_key)` + `request_hash` chống tạo booking trùng.
5. **TTL & Expiry** — Booking `PENDING` hết hạn sau 10 phút; scheduler tự động chuyển sang `EXPIRED` và trả ghế về `AVAILABLE`.
6. **Atomic Transactions** — Toàn bộ ghế được giữ hoặc không giữ ghế nào (all-or-nothing).

---

## Testing

```bash
# Chạy tất cả tests từ root
mvn test

# Hoặc chỉ backend
cd backend && mvn test
```

Bao gồm:
- **Unit tests** — Validation nghiệp vụ, tính toán giá, state transitions.
- **Integration tests** — Repository, Flyway migration trên PostgreSQL.
- **Concurrency tests** — 20 request đồng thời đặt cùng ghế, deadlock prevention.
- **API tests** — Status code, error code, không lộ stack trace.

---

## Tài khoản demo

Migration V8 tạo sẵn bộ dữ liệu demo. Tất cả tài khoản dùng chung mật khẩu:

```
Cinema@123
```

| Vai trò | Email |
|---|---|
| Admin | `admin@cinema.local` |
| Staff | `manager.hcm@cinema.local` |
| Staff | `manager.hanoi@cinema.local` |
| User | `customer01@cinema.local` → `customer30@cinema.local` |

> `locked.user@cinema.local` và `deleted.user@cinema.local` chỉ dùng kiểm thử trạng thái, không đăng nhập được.

---

## Tài liệu tham khảo

| Tài liệu | Đường dẫn |
|---|---|
| Thiết kế Phase 2 (Booking + Concurrency) | [`docs/PHASE_2_README.md`](docs/PHASE_2_README.md) |
| ERD toàn bộ database | [`docs/database-erd.md`](docs/database-erd.md) |
| Mô tả dữ liệu mẫu | [`docs/SAMPLE_DATA.md`](docs/SAMPLE_DATA.md) |
| Swagger UI (khi backend đang chạy) | `http://localhost:8080/swagger-ui.html` |
