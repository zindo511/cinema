package vn.cinema.domain.common.exception;

public class RefreshTokenExpiredException extends BusinessRuleException{
    public RefreshTokenExpiredException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
