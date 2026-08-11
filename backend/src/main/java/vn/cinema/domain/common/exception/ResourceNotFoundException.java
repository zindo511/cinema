package vn.cinema.domain.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BusinessRuleException {
    public ResourceNotFoundException(String message) {
        super(BusinessErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
