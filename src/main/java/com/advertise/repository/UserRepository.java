package com.advertise.repository;

import com.advertise.entity.User;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
@Validated
public interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findByNameEquals(@NotBlank String username);
	Optional<User> findByEmailEquals(@NotBlank String email);
}
