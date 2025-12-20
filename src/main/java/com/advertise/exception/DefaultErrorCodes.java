package com.advertise.exception;

import io.micronaut.http.HttpStatus;

public enum DefaultErrorCodes implements ExceptionCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    CONSTRAINT_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY),
    USER_LOCKED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    URL_NOT_FOUND(HttpStatus.NOT_FOUND),
    OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND),
    CONCURRENT_MODIFICATION(HttpStatus.PRECONDITION_FAILED),
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY),
    VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY),
    TIMEOUT(HttpStatus.REQUEST_TIMEOUT),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INVALID_PARAMETER_VALUE(HttpStatus.UNPROCESSABLE_ENTITY),
    STALE_OBJECT_STATE(HttpStatus.CONFLICT),

    //Server side errors - HTTP 500-
    TECHNICAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY),
    NO_PERMISSION(HttpStatus.FORBIDDEN);

    private final HttpStatus httpStatus;

    DefaultErrorCodes(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public int getHttpStatusCode() {
        return httpStatus.getCode();
    }
}
