package com.advertise.entity;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;

@MappedEntity("users")
@Serdeable
public record User (
        @Id @GeneratedValue Long id,
        @NonNull String email,
        @NonNull String name,
        @NonNull String passwordHash,
        @NonNull Instant createdAt
) {
}
