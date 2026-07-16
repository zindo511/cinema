package vn.cinema.domain.common.exception;

public class ConflictException extends BusinessRuleException {

    public ConflictException(String message) {
        super(BusinessErrorCode.CONFLICT, message);
    }

    public ConflictException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }
}
