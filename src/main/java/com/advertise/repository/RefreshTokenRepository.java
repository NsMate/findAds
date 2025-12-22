package com.advertise.repository;

import com.advertise.entity.RefreshToken;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
@Validated
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(@NonNull @NotBlank String refreshToken);

    Optional<RefreshToken> findByUsername(@NonNull @NotBlank String username);

    long deleteByDateCreatedBefore(@NonNull Instant createdAt);
}
