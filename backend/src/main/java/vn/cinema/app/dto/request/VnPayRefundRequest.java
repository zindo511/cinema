package vn.cinema.app.dto.request;

public record VnPayRefundRequest(
        String vnp_RequestId,
        String vnp_Version,
        String vnp_Command,
        String vnp_TmnCode,
        String vnp_TransactionType,
        String vnp_TxnRef,
        long vnp_Amount,
        String vnp_OrderInfo,
        String vnp_TransactionNo,
        String vnp_TransactionDate,
        String vnp_CreateBy,
        String vnp_CreateDate,
        String vnp_IpAddr,
        String vnp_SecureHash
) {
}
