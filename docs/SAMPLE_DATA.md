# Dữ liệu mẫu

Migration `V8__seed_sample_data.sql` tự động tạo bộ dữ liệu demo lớn khi Flyway chạy trên database chưa áp dụng V8.

## Quy mô dữ liệu

- 15 thể loại và 30 phim, bao gồm đủ trạng thái sắp chiếu, đang chiếu, đã kết thúc và xóa mềm.
- 7 rạp tại nhiều thành phố, 21 phòng chiếu và khoảng 2.500 ghế vật lý với ghế thường, VIP, couple và một số ghế bảo trì.
- Hơn 700 suất chiếu trong quá khứ và tương lai; mỗi suất có đầy đủ snapshot ghế và giá.
- 36 tài khoản thuộc các vai trò user, staff, admin và các trạng thái active, locked, deleted.
- Khoảng 640 booking, hơn 1.200 booking-seat và hơn 700 lượt thanh toán, phủ các trạng thái pending, confirmed, cancelled, expired, success và failed.
- 12 refresh token mẫu đã hết hạn và bị thu hồi; không có raw token sử dụng được trong source code.

Số lượng ghế và suất chiếu thực tế có thể chênh nhẹ do ghế couple chiếm hai vị trí và một số ghế được đánh dấu bảo trì.

## Tài khoản đăng nhập

Các tài khoản mẫu đang hoạt động dùng chung mật khẩu:

```text
Cinema@123
```

Tài khoản tiêu biểu:

| Vai trò | Email |
|---|---|
| Admin | `admin@cinema.local` |
| Staff | `manager.hcm@cinema.local` |
| Staff | `manager.hanoi@cinema.local` |
| User | `customer01@cinema.local` đến `customer30@cinema.local` |

`locked.user@cinema.local` và `deleted.user@cinema.local` chỉ phục vụ kiểm thử trạng thái, không đăng nhập được.

## Lưu ý

- Thời gian suất chiếu được tính tương đối theo ngày chạy migration, với múi giờ `Asia/Ho_Chi_Minh`.
- Các URL poster dùng ảnh placeholder để môi trường demo không phụ thuộc dữ liệu ảnh thật.
- Giá ghế được tính từ giá cơ bản theo hệ số: thường `1.0`, VIP `1.5`, couple `2.0`.
- Các dòng có cột audit `created_by = -8` là dữ liệu được tạo bởi migration seed.
