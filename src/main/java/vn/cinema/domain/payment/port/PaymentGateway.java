package vn.cinema.domain.payment.port;

import java.math.BigDecimal;

public interface PaymentGateway {

    String createPaymentUrl(BigDecimal amount, String orderInfo, String ipAddress, String txnRef);
}
