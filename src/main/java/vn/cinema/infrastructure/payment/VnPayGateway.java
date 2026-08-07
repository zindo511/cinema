package vn.cinema.infrastructure.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
}
