package com.advertise.security;

import com.advertise.entity.User;
import com.advertise.repository.UserRepository;
import com.advertise.security.encoder.BCryptPasswordEncoderService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestReactiveAuthenticationProvider;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Singleton
public class AuthenticationProviderService<B> implements HttpRequestReactiveAuthenticationProvider<B> {

	private final UserRepository users;
	private final BCryptPasswordEncoderService passwordEncoder;

	public AuthenticationProviderService(UserRepository users, BCryptPasswordEncoderService passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<B> httpRequest,
			AuthenticationRequest<String, String> authRequest) {

		return Mono.fromCallable(() -> {
			String username = authRequest.getIdentity();
			String password = authRequest.getSecret();

			User user = users.findByNameEquals(username).orElse(null);

			if (user == null) {
				passwordEncoder.matches(password, "$2a$10$dummyHashToPreventTimingAttacks");
				return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
			}

			if (!passwordEncoder.matches(password, user.passwordHash())) {
				return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
			}

			return AuthenticationResponse.success(username);
		});
	}
}
