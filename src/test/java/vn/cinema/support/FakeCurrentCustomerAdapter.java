package vn.cinema.support;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import vn.cinema.domain.booking.port.CurrentCustomerPort;

@Component
@Primary
@Profile("test")
public class FakeCurrentCustomerAdapter implements CurrentCustomerPort {

    private Long currentCustomerId = 1L;

    @Override
    public Long getCurrentCustomerId() {
        return currentCustomerId;
    }

    public void setCurrentCustomerId(Long currentCustomerId) {
        this.currentCustomerId = currentCustomerId;
    }
}
