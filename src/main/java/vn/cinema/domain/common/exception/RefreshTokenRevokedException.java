package vn.cinema.domain.common.exception;

public class RefreshTokenRevokedException extends BusinessRuleException {

    public RefreshTokenRevokedException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
