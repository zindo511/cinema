package vn.cinema.app.adapter.customer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import vn.cinema.domain.booking.port.CurrentCustomerPort;
import vn.cinema.domain.common.exception.AuthenticationRequiredException;
import vn.cinema.domain.common.exception.BusinessErrorCode;

@Component
@Profile("!local & !test")
public class UnauthenticatedCurrentCustomerAdapter implements CurrentCustomerPort {

    @Override
    public Long getCurrentCustomerId() {
        throw new AuthenticationRequiredException(
                BusinessErrorCode.CURRENT_CUSTOMER_REQUIRED,
                "Authentication is required to create a booking"
        );
    }
}
