package vn.cinema.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotBlank(message = "Nội dung thanh toán không được để trống")
    @Size(max = 200, message = "Nội dung thanh toán không được vượt quá 255 ký tự")
    @Pattern(
            regexp = "^[A-Za-z0-9 ]+$",
            message = "Nội dung thanh toán chỉ được chứa chữ không dấu, số và khoảng trắng"
    )
    private String orderInfo;
}
