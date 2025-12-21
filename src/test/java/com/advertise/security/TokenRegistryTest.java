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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class TokenRegistryTest {

    @Inject @Client("/") HttpClient client;
    @Inject UserRepository users;
    @Inject RefreshTokenRepository tokens;
    @Inject PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        tokens.deleteAll();
        users.deleteAll();
        users.save(new User(null, "alice@test.com", "alice", encoder.encode("pass"), Instant.now()));
    }

    @Test
    void refreshTokenCreatesNewAccessToken() throws InterruptedException {
        Cookie oldAccess = login("alice", "pass");
        Cookie refresh = getRefreshToken();

        Thread.sleep(1000);

        Cookie newAccess = refresh(refresh);

        assertNotEquals(oldAccess.getValue(), newAccess.getValue());
        assertTrue(canAccessProtectedEndpoint(newAccess));
    }

    @Test
    void revokedTokenFails() {
        Cookie refresh = login("alice", "pass");
        tokens.deleteAll();

        assertThrows(Exception.class, () -> refresh(getRefreshToken()));
    }

    private Cookie login(String username, String password) {
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.POST("/login", Map.of("username", username, "password", password))
                        .header("X-Client-Type", "web"),
                Map.class
        );
        return response.getCookie("access_token").orElseThrow();
    }

    private Cookie getRefreshToken() {
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.POST("/login", Map.of("username", "alice", "password", "pass"))
                        .header("X-Client-Type", "web"),
                Map.class
        );
        return response.getCookie("refresh_token").orElseThrow();
    }

    private Cookie refresh(Cookie refreshToken) {
        HttpResponse<Map> response = client.toBlocking().exchange(
                HttpRequest.POST("/auth/access_token",
                                Map.of("grant_type", "refresh_token", "refresh_token", refreshToken.getValue()))
                        .header("X-Client-Type", "web"),
                Map.class
        );
        return response.getCookie("access_token").orElseThrow();
    }

    private boolean canAccessProtectedEndpoint(Cookie accessToken) {
        try {
            HttpResponse<String> response = client.toBlocking().exchange(
                    HttpRequest.GET("/hello").cookie(accessToken),
                    String.class
            );
            return response.status() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
}
