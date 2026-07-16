package vn.cinema.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.cinema.domain.common.exception.AuthenticationRequiredException;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.BusinessRuleException;
import vn.cinema.domain.common.exception.ConflictException;
import vn.cinema.domain.common.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Not Found", ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationRequired(AuthenticationRequiredException ex) {
        return error(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, "Conflict", ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        return error(HttpStatus.BAD_REQUEST, "Bad Request", ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                BusinessErrorCode.INVALID_REQUEST.name(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        return error(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                BusinessErrorCode.VALIDATION_ERROR.name(),
                message
        );
    }

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status,
            String error,
            String code,
            String message
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("code", code);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
