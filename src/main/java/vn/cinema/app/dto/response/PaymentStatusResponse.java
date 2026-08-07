package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vn.cinema.domain.booking.entity.BookingStatus;
import vn.cinema.domain.payment.entity.PaymentStatus;

@Getter
@Builder
@AllArgsConstructor
public class PaymentStatusResponse {

    private String paymentReference;
    private PaymentStatus paymentStatus;
    private BookingStatus bookingStatus;
}
