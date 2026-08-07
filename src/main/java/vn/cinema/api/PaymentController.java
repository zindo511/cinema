package vn.cinema.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.response.*;
import vn.cinema.app.service.PaymentService;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<VnPayIpnResponse> handleVnPayIpn(
            @RequestParam Map<String, String> params
    ) {
        return ResponseEntity.ok(paymentService.handleIpnPayment(params));
    }

    @GetMapping("/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @RequestParam String txnRef
    ) {
        return ResponseEntity.ok(paymentService.getStatus(txnRef));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentSummaryResponse>> getPaymentsHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = ((Number) Objects.requireNonNull(jwt.getClaim("userId"))).longValue();
        return ResponseEntity.ok(PageResponse.from(paymentService.getPayments(userId, page, size)));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponse> getPaymentDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long paymentId
    ) {
        Long userId = ((Number) Objects.requireNonNull(jwt.getClaim("userId"))).longValue();
        return ResponseEntity.ok(paymentService.getPaymentDetail(userId, paymentId));
    }
}
