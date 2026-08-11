package vn.cinema.domain.common.exception;

public class CancellationNotAllowedException extends BusinessRuleException {
    public CancellationNotAllowedException(String message) {
        super(BusinessErrorCode.CANCELLATION_NOT_ALLOWED, message);
    }
}
