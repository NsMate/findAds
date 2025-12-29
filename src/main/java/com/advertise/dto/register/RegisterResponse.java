package com.advertise.dto.register;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record RegisterResponse(String username, String email, String message) {
}
