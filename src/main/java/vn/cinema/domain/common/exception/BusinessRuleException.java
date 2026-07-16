package vn.cinema.domain.common.exception;

import lombok.Getter;

import java.util.Objects;

@Getter
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String message) {
        this(BusinessErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public BusinessRuleException(BusinessErrorCode errorCode, String message) {
        this(errorCode.name(), message);
    }

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    public String getCode() {
        return errorCode;
    }
}
