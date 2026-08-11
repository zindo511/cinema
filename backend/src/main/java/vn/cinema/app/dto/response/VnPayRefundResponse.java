package vn.cinema.app.dto.response;

public record VnPayRefundResponse(
        String vnp_ResponseId,        // Mã định danh (duy nhất) của cục response này do hệ thống VNPay sinh ra
        String vnp_Command,           // Luôn là "refund" (để xác nhận lại đúng loại API)
        String vnp_ResponseCode,      // QUAN TRỌNG NHẤT: Mã phản hồi ("00" là thành công, khác "00" là có lỗi)
        String vnp_Message,           // Thông báo lỗi chi tiết (vd: "Transaction not found", "Success")
        String vnp_TmnCode,           // Mã merchant của bạn (để kiểm tra chéo)
        String vnp_TxnRef,            // Mã đơn hàng gốc mà bạn yêu cầu hoàn tiền
        String vnp_Amount,            // Số tiền được hoàn (Vẫn chứa 2 số 0 ở cuối, ví dụ 1000000 = 10k VND)
        String vnp_OrderInfo,         // Nội dung mô tả (bạn gửi lên gì VNPay trả về nấy)
        String vnp_BankCode,          // Mã ngân hàng của giao dịch gốc (VNPAYQR, NCB,...)
        String vnp_PayDate,           // Ngày giờ thanh toán của giao dịch gốc
        String vnp_TransactionNo,     // Mã giao dịch nội bộ của hệ thống VNPay
        String vnp_TransactionType,   // Mã loại hoàn tiền ("02" - hoàn toàn phần)
        String vnp_TransactionStatus, // Trạng thái của giao dịch trên hệ thống VNPay
        String vnp_SecureHash         // Chữ ký điện tử VNPay tự tạo ra để bạn xác thực (verify) ngược lại xem có đúng phản hồi từ VNPay không
) {
}
