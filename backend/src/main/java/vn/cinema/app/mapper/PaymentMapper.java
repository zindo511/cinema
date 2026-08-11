package vn.cinema.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.cinema.app.dto.response.PaymentDetailResponse;
import vn.cinema.app.dto.response.PaymentSummaryResponse;
import vn.cinema.domain.payment.entity.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentSummaryResponse toSummaryResponse(Payment payment);

    @Mapping(target = "bookingId", source = "booking.id")
    PaymentDetailResponse toDetailResponse(Payment payment);
}
