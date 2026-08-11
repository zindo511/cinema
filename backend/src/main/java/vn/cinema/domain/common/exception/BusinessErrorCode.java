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
    BOOKING_EXPIRATION_NOT_REACHED,
    BOOKING_EXPIRED,
    SHOWTIME_NOT_BOOKABLE,
    REFRESH_TOKEN_REVOKED,
    REFRESH_TOKEN_EXPIRED,
    SEAT_NOT_AVAILABLE,
    IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD,
    CANCELLATION_NOT_ALLOWED
}
