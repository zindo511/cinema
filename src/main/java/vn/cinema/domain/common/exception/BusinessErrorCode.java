package vn.cinema.domain.common.exception;

/**
 * Stable machine-readable codes used by API clients. Messages may change; these values must not.
 */
public enum BusinessErrorCode {
    RESOURCE_NOT_FOUND,
    INVALID_REQUEST,
    VALIDATION_ERROR,
    BUSINESS_RULE_VIOLATION,
    CONFLICT,
    INVALID_BOOKING_STATUS,
    BOOKING_CREATION_CONFLICT,
    SHOWTIME_NOT_BOOKABLE,
    SEAT_NOT_AVAILABLE,
    IDEMPOTENCY_KEY_REUSED,
    CURRENT_CUSTOMER_REQUIRED,
    CURRENT_CUSTOMER_INVALID,
    REFRESH_TOKEN_REVOKED,
    REFRESH_TOKEN_EXPIRED
}
