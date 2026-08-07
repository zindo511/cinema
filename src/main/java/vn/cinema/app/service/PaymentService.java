package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.request.PaymentRequest;
import vn.cinema.app.dto.response.*;
import vn.cinema.app.mapper.PaymentMapper;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.entity.BookingStatus;
import vn.cinema.domain.booking.entity.Ticket;
import vn.cinema.domain.booking.repository.BookingRepository;
import vn.cinema.domain.booking.repository.TicketRepository;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.payment.entity.Payment;
import vn.cinema.domain.payment.entity.PaymentStatus;
import vn.cinema.domain.payment.port.PaymentGateway;
import vn.cinema.domain.payment.repository.PaymentRepository;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;
import vn.cinema.domain.showtime.repository.ShowtimeSeatRepository;
import vn.cinema.infrastructure.payment.VnPayChecksumVerifier;
import vn.cinema.infrastructure.utility.TxnRef;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Clock clock;
    private final PaymentGateway paymentGateway;
    private final BookingRepository bookingRepository;
    private final VnPayChecksumVerifier vnPayChecksumVerifier;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final TicketRepository ticketRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public CreatePaymentResponse createVnPayPayment(
            Long customerId,
            Long bookingId,
            PaymentRequest request,
            String ipAddress) {
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Booking PENDING và chưa hết hạn
        booking.ensurePayable(clock.instant());

        // Tạo Payment
        Optional<Payment> pendingPayment = paymentRepository.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
                bookingId, PaymentStatus.PENDING);

        // Ko tạo Payment mới, huỷ Payment cũ, tạo mới
        pendingPayment.ifPresent(old -> old.markFailed("Superseded by new payment attempt"));

        // Nếu chưa có, thì tạo Payment mới
        String txnRef = TxnRef.generateTxnRef();
        Payment payment = createPaymentPending(booking, txnRef);
        paymentRepository.save(payment);

        String paymentUrl = paymentGateway.createPaymentUrl(
                booking.getTotalAmount(), request.getOrderInfo(), ipAddress, txnRef, booking.getExpiresAt());
        return toResponse(payment, paymentUrl);
    }

    @Transactional
    public VnPayIpnResponse handleIpnPayment(Map<String, String> params) {
        try {
            log.info("Received VNPay IPN callback with params: {}", params);
            // verify param --> hash = hash VnPay gửi về
            if (!vnPayChecksumVerifier.verify(params)) {
                return ipnResponse("97", "Invalid Signature");
            }

            String paymentReference = params.get("vnp_TxnRef");

            // tìm payment và khoá bản ghi
            Payment payment = paymentRepository.findByPaymentReferenceForUpdate(paymentReference)
                    .orElse(null);

            if (payment == null) {
                return ipnResponse("01", "Order not found");
            }

            // xử lý trường hợp retry
            if (!payment.isPending()) {
                return ipnResponse("02", "Order already confirmed");
            }

            BigDecimal receivedAmount = new BigDecimal(params.get("vnp_Amount"));
            BigDecimal expectedAmount = payment.getAmount().multiply(BigDecimal.valueOf(100));
            if (receivedAmount.compareTo(expectedAmount) != 0) {
                return ipnResponse("04", "Invalid Amount");
            }

            // xác định kết quả thanh toán
            String responseCode = params.get("vnp_ResponseCode");
            String transactionStatus = params.get("vnp_TransactionStatus");
            boolean successful = "00".equals(responseCode)
                    && "00".equals(transactionStatus);

            if (successful) {
                payment.markSuccess(
                        params.get("vnp_TransactionNo"),
                        responseCode,
                        params.get("vnp_BankCode"),
                        parsePayDate(params.get("vnp_PayDate")));

                // xử lý booking: PENDING -> CONFIRMED và showtime_seat: HELD -> BOOKED
                Booking booking = bookingRepository.findByIdForUpdate(payment.getBooking().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Booking not found with id: " + payment.getBooking().getId()));

                // kiểm tra Booking còn hợp lệ để confirm hay ko
                boolean isExpired = booking.getStatus() == BookingStatus.EXPIRED
                        || !booking.getExpiresAt().isAfter(clock.instant());

                if (isExpired) {
                    // Booking hết hạn → KHÔNG confirm vé, đánh dấu hoàn tiền
                    payment.markRefundPending("Booking expired before payment completed");
                    log.warn("[IPN-EXPIRED] txnRef={}, bookingId={} expired. Payment marked REFUND_PENDING.",
                            paymentReference, booking.getId());
                }

                else {
                    // confirm vé
                    booking.confirm();
                    List<ShowtimeSeat> seats = showtimeSeatRepository.findAllByBookingIdForUpdate(booking.getId());
                    seats.forEach(ShowtimeSeat::book);
                    // sinh ticket
                    List<Ticket> tickets = booking.getBookingSeats().stream()
                            .map(bookingSeat -> Ticket.builder()
                                    .bookingSeat(bookingSeat)
                                    .build()
                            ).toList();
                    ticketRepository.saveAll(tickets);
                    log.info("[IPN-SUCCESS] Successfully confirmed bookingId={} via txnRef={}", booking.getId(),
                            paymentReference);
                }
            } else {
                payment.markFailed(responseCode, buildFailureReason(responseCode));
                log.warn("[IPN-FAILED] Payment failed at VNPay for txnRef={}, responseCode={}", paymentReference,
                        responseCode);
            }

            return ipnResponse("00", "Confirm Success");
        } catch (Exception e) {
            log.error("Error processing VNPay IPN: {}", params, e);
            return ipnResponse("99", "Unknown error");
        }
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getStatus(String txnRef) {
        Payment payment = paymentRepository.findByPaymentReference(txnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with txnRef: " + txnRef));

        return PaymentStatusResponse.builder()
                .paymentReference(txnRef)
                .paymentStatus(payment.getStatus())
                .bookingStatus(payment.getBooking().getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PaymentSummaryResponse> getPayments(Long userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Payment> payments = paymentRepository.findAllByPaymentUserId(userId, pageable);
        return payments.map(paymentMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetail(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByPaymentUserId(userId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        return paymentMapper.toDetailResponse(payment);
    }

    // PRIVATE HELPER
    private CreatePaymentResponse toResponse(Payment payment, String paymentUrl) {
        return CreatePaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .paymentUrl(paymentUrl)
                .build();
    }

    private Payment createPaymentPending(Booking booking, String txnRef) {
        return Payment.builder()
                .booking(booking)
                .paymentReference(txnRef)
                .provider("VNPAY")
                .status(PaymentStatus.PENDING)
                .amount(booking.getTotalAmount())
                .build();
    }

    private String buildFailureReason(String responseCode) {
        return "VNPay payment failed, responseCode=" + responseCode;
    }

    private Instant parsePayDate(String payDate) {
        if (payDate == null || payDate.isBlank()) {
            return Instant.now();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        LocalDateTime localDateTime = LocalDateTime.parse(payDate, formatter);

        return localDateTime
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toInstant();
    }

    private VnPayIpnResponse ipnResponse(String rspCode, String message) {
        return VnPayIpnResponse.builder()
                .rspCode(rspCode)
                .message(message)
                .build();
    }
}
