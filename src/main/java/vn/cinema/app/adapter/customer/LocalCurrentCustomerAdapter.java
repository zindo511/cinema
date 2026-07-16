package vn.cinema.app.adapter.customer;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import vn.cinema.domain.booking.port.CurrentCustomerPort;
import vn.cinema.domain.common.exception.AuthenticationRequiredException;
import vn.cinema.domain.common.exception.BusinessErrorCode;

@Component
@Profile("local")
@RequestScope
public class LocalCurrentCustomerAdapter implements CurrentCustomerPort {

    public static final String CUSTOMER_ID_HEADER = "X-Customer-Id";

    private final HttpServletRequest request;

    public LocalCurrentCustomerAdapter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Long getCurrentCustomerId() {
        String rawCustomerId = request.getHeader(CUSTOMER_ID_HEADER);
        if (rawCustomerId == null || rawCustomerId.isBlank()) {
            throw new AuthenticationRequiredException(
                    BusinessErrorCode.CURRENT_CUSTOMER_REQUIRED,
                    CUSTOMER_ID_HEADER + " header is required in the local profile"
            );
        }

        try {
            long customerId = Long.parseLong(rawCustomerId.trim());
            if (customerId <= 0) {
                throw invalidCustomerId();
            }
            return customerId;
        } catch (NumberFormatException ex) {
            throw invalidCustomerId();
        }
    }

    private AuthenticationRequiredException invalidCustomerId() {
        return new AuthenticationRequiredException(
                BusinessErrorCode.CURRENT_CUSTOMER_INVALID,
                CUSTOMER_ID_HEADER + " must be a positive integer"
        );
    }
}
