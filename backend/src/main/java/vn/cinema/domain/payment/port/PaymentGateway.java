package vn.cinema.domain.payment.port;

import vn.cinema.app.dto.request.RefundCommand;
import vn.cinema.app.dto.response.RefundResult;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentGateway {

    String createPaymentUrl(BigDecimal amount, String orderInfo, String ipAddress, String txnRef, Instant expiresAt);

    RefundResult refund(RefundCommand command);
}
