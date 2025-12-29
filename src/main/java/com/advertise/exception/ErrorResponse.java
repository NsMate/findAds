package com.advertise.exception;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ErrorResponse(ExceptionCode code, String message) {
}
