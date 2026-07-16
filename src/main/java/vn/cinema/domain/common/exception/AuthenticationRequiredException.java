package vn.cinema.domain.common.exception;

public class AuthenticationRequiredException extends BusinessRuleException {

    public AuthenticationRequiredException(String message) {
        super(BusinessErrorCode.CURRENT_CUSTOMER_REQUIRED, message);
    }

    public AuthenticationRequiredException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
