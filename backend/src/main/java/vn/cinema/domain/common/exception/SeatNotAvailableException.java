package vn.cinema.domain.common.exception;

public class SeatNotAvailableException extends BusinessRuleException {
    public SeatNotAvailableException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
