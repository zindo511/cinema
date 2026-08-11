package vn.cinema.infrastructure.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import vn.cinema.app.dto.request.RefundCommand;
import vn.cinema.app.dto.request.VnPayRefundRequest;
import vn.cinema.app.dto.response.RefundResult;
import vn.cinema.app.dto.response.VnPayRefundResponse;
import vn.cinema.config.VnPayProperties;
import vn.cinema.domain.payment.port.PaymentGateway;
import vn.cinema.infrastructure.utility.VnPayUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VnPayGateway implements PaymentGateway {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayProperties vnPayProperties;
    private final RestClient restClient = RestClient.create();

    @Override
    public String createPaymentUrl(BigDecimal amount, String orderInfo, String ipAddress, String txnRef, Instant expiresAt) {
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnPayProperties.getVersion());
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .toPlainString()
        );
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan booking " + orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");

        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", now.format(FORMATTER));

        LocalDateTime expireDate = LocalDateTime.ofInstant(expiresAt, VN_ZONE);
        params.put("vnp_ExpireDate", expireDate.format(FORMATTER));

        String query = VnPayUtil.buildQueryUrl(params);

        String secureHash = VnPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), query);

        return vnPayProperties.getPayUrl()
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;
    }

    @Override
    public RefundResult refund(RefundCommand command) {
        String requestId = command.refundReference();

        String transactionType = "02"; // full refund

        long amount = command.amount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        // 1. Format các trường thời gian
        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        String createDate = now.format(FORMATTER);

        // Thời điểm thanh toán lúc đầu
        LocalDateTime txnDate = LocalDateTime.ofInstant(command.paymentCreatedAt(), VN_ZONE);
        String transactionDate = txnDate.format(FORMATTER);
        String orderInfo = "Hoan tien giao dich " + command.paymentReference();
        String ipAddress = "127.0.0.1";

        String dataToHash = String.join("|",
                requestId,
                vnPayProperties.getVersion(),
                "refund",
                vnPayProperties.getTmnCode(),
                transactionType,
                command.paymentReference(),
                String.valueOf(amount),
                command.providerTransactionNo(),
                transactionDate,
                command.createdBy(),
                createDate,
                ipAddress,
                orderInfo
        );

        // tạo secure hash
        String secureHash = VnPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), dataToHash);

        VnPayRefundRequest request = new VnPayRefundRequest(
                requestId,
                vnPayProperties.getVersion(),
                "refund",
                vnPayProperties.getTmnCode(),
                transactionType,
                command.paymentReference(),
                amount,
                orderInfo,
                command.providerTransactionNo(),
                transactionDate,
                command.createdBy(),
                createDate,
                ipAddress,
                secureHash
        );

        // post json tới vnpay
        VnPayRefundResponse response = restClient.post()
                .uri(vnPayProperties.getRefundUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(VnPayRefundResponse.class);

        if (response == null) {
            throw new IllegalStateException("VNPAY returned empty refund response");
        }

        return new RefundResult(
                response.vnp_ResponseId(),
                response.vnp_TransactionNo(),
                response.vnp_ResponseCode(),
                response.vnp_TransactionStatus(),
                response.vnp_Message()
        );
    }
}
