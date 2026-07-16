package vn.cinema.app.adapter.customer;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import vn.cinema.domain.common.exception.AuthenticationRequiredException;
import vn.cinema.domain.common.exception.BusinessErrorCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalCurrentCustomerAdapterTests {

    @Test
    void readsCustomerIdFromLocalHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(LocalCurrentCustomerAdapter.CUSTOMER_ID_HEADER, " 42 ");

        Long customerId = new LocalCurrentCustomerAdapter(request).getCurrentCustomerId();

        assertEquals(42L, customerId);
    }

    @Test
    void rejectsMissingCustomerHeader() {
        LocalCurrentCustomerAdapter adapter = new LocalCurrentCustomerAdapter(new MockHttpServletRequest());

        AuthenticationRequiredException exception = assertThrows(
                AuthenticationRequiredException.class,
                adapter::getCurrentCustomerId
        );

        assertEquals(BusinessErrorCode.CURRENT_CUSTOMER_REQUIRED.name(), exception.getCode());
    }

    @Test
    void rejectsInvalidCustomerHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(LocalCurrentCustomerAdapter.CUSTOMER_ID_HEADER, "not-a-customer");

        AuthenticationRequiredException exception = assertThrows(
                AuthenticationRequiredException.class,
                () -> new LocalCurrentCustomerAdapter(request).getCurrentCustomerId()
        );

        assertEquals(BusinessErrorCode.CURRENT_CUSTOMER_INVALID.name(), exception.getCode());
    }
}
