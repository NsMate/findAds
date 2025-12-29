package com.advertise.security;

import com.advertise.entity.User;
import com.advertise.repository.RefreshTokenRepository;
import com.advertise.repository.UserRepository;
import com.advertise.security.encoder.PasswordEncoder;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
class TokenRegistryTest {

	@Inject
	@Client("/")
	HttpClient client;
	@Inject
	UserRepository users;
	@Inject
	RefreshTokenRepository refreshTokenRepository;
	@Inject
	PasswordEncoder encoder;

	@BeforeEach
	void setup() {
		refreshTokenRepository.deleteAll();
		users.deleteAll();
		users.save(new User(null, "alice@test.com", "alice", encoder.encode("pass"), Instant.now()));
	}

	@Test
	void refreshTokenCreatesNewAccessToken() throws InterruptedException {
		String oldAccess = login("alice", "pass");
		Cookie refresh = getRefreshToken();

		Thread.sleep(1000);

		String newAccess = refresh(refresh);

		assertNotEquals(oldAccess, newAccess);
		assertTrue(canAccessProtectedEndpoint(newAccess));
	}

	@Test
	void revokedTokenFails() {
		Cookie token = getRefreshToken();

		refreshTokenRepository.deleteAll();
		assertThrows(Exception.class, () -> refresh(token));
	}

	private String login(String username, String password) {
		HttpResponse<?> response = client.toBlocking().exchange(HttpRequest
				.POST("/login", Map.of("username", username, "password", password)).header("X-Client-Type", "web"),
				Map.class);
		Optional<LinkedHashMap> bodyOpt = response.getBody(LinkedHashMap.class);

		if (bodyOpt.isPresent()) {
			return (String) bodyOpt.get().get("access_token");
		}

		return "";
	}

	private Cookie getRefreshToken() {
		HttpResponse<?> response = client.toBlocking().exchange(HttpRequest
				.POST("/login", Map.of("username", "alice", "password", "pass")).header("X-Client-Type", "web"),
				Map.class);
		return response.getCookie("refresh_token").orElseThrow();
	}

	private String refresh(Cookie refreshToken) {
		HttpResponse<Map> response = client.toBlocking()
				.exchange(HttpRequest
						.POST("/oauth/access_token",
								Map.of("grant_type", "refresh_token", "refresh_token", refreshToken.getValue()))
						.header("X-Client-Type", "web"), Map.class);
		Optional<LinkedHashMap> bodyOpt = response.getBody(LinkedHashMap.class);

		if (bodyOpt.isPresent()) {
			return (String) bodyOpt.get().get("access_token");
		}

		return "";
	}

	private boolean canAccessProtectedEndpoint(String accessToken) {
		try {
			HttpResponse<String> response = client.toBlocking()
					.exchange(HttpRequest.GET("/hello").bearerAuth(accessToken), String.class);
			return response.status() == HttpStatus.OK;
		} catch (Exception e) {
			return false;
		}
	}
}
