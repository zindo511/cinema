package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.request.PaymentRequest;
import vn.cinema.app.dto.response.CreatePaymentResponse;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.repository.BookingRepository;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.payment.entity.Payment;
import vn.cinema.domain.payment.entity.PaymentStatus;
import vn.cinema.domain.payment.port.PaymentGateway;
import vn.cinema.domain.payment.repository.PaymentRepository;
import vn.cinema.infrastructure.utility.TxnRef;

import java.time.Clock;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Clock clock;
    private final PaymentGateway paymentGateway;
    private final BookingRepository bookingRepository;

    @Transactional
    public CreatePaymentResponse createVnPayPayment(
            Long customerId,
            Long bookingId,
            PaymentRequest request,
            String ipAddress
    ) {
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Booking PENDING và chưa hết hạn
        booking.ensurePayable(clock.instant());

        // Tạo Payment 
        Optional<Payment> pendingPayment = paymentRepository.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
                bookingId, PaymentStatus.PENDING
        );

        // Ko tạo Payment mới, huỷ Payment cũ, tạo mới
        pendingPayment.ifPresent(old -> old.markFailed("Superseded by new payment attempt"));

        // Nếu chưa có, thì tạo Payment mới
        String txnRef = TxnRef.generateTxnRef();
        Payment payment = createPaymentPending(booking, txnRef);
        paymentRepository.save(payment);

        String paymentUrl = paymentGateway.createPaymentUrl(
                booking.getTotalAmount(), request.getOrderInfo(), ipAddress, txnRef
        );
        return toResponse(payment, paymentUrl);
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
}
