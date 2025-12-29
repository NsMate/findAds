package com.advertise.entity;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@MappedEntity("refresh_token")
public record RefreshToken(
    @Id @GeneratedValue @NonNull Long id,
    @NonNull @NotBlank String username,
    @NonNull @NotBlank String refreshToken,
    @NonNull @NotNull Boolean revoked,
    @DateCreated @NonNull @NotNull Instant dateCreated
) {}
