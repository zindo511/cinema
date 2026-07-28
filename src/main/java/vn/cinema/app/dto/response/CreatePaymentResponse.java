package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreatePaymentResponse {
    private Long paymentId;
    private String paymentReference;
    private String paymentUrl;
}
