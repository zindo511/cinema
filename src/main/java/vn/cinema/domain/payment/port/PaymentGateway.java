package vn.cinema.domain.payment.port;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentGateway {

    String createPaymentUrl(BigDecimal amount, String orderInfo, String ipAddress, String txnRef, Instant expiresAt);
}
