package com.advertise.security;

import com.advertise.entity.User;
import com.advertise.repository.RefreshTokenRepository;
import com.advertise.repository.UserRepository;
import com.advertise.security.encoder.PasswordEncoder;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.endpoints.TokenRefreshRequest;
import io.micronaut.security.token.generator.RefreshTokenGenerator;
import io.micronaut.security.token.render.AccessRefreshToken;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class TokenRegistryTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordEncoder passwordEncoder;

    @Inject
    RefreshTokenGenerator refreshTokenGenerator;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        userRepository.save(new User(null, "test@test.com","alice",
                passwordEncoder.encode("testPass"), Instant.now()));
    }

    @Test
    void accessingSecuredURLWithoutAuthenticatingReturnsUnauthorized() {
        Authentication user = Authentication.build("alice");

        String refreshToken = refreshTokenGenerator.createKey(user);
        Optional<String> refreshTokenOptional = refreshTokenGenerator.generate(user, refreshToken);
        assertTrue(refreshTokenOptional.isPresent());

        String signedRefreshToken = refreshTokenOptional.get();
        Argument<BearerAccessRefreshToken> bodyArgument = Argument.of(BearerAccessRefreshToken.class);
        Argument<Map> errorArgument = Argument.of(Map.class);
        HttpRequest<?> req = HttpRequest.POST("/oauth/access_token", new TokenRefreshRequest(TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN, signedRefreshToken));

        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(req, bodyArgument, errorArgument);
        });
        assertEquals(BAD_REQUEST, e.getStatus());

        Optional<Map> mapOptional = e.getResponse().getBody(Map.class);
        assertTrue(mapOptional.isPresent());

        Map m = mapOptional.get();
        assertEquals("invalid_grant", m.get("error"));
        assertEquals("refresh token not found", m.get("error_description"));
    }

    @Test
    void accessingSecuredURLWithoutAuthenticatingReturnsUnauthorizedUnsignedToken() {
        String unsignedRefreshedToken = "foo";

        Argument<BearerAccessRefreshToken> bodyArgument = Argument.of(BearerAccessRefreshToken.class);
        Argument<Map> errorArgument = Argument.of(Map.class);

        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.POST("/oauth/access_token", new TokenRefreshRequest(TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN, unsignedRefreshedToken)),
                    bodyArgument,
                    errorArgument);
        });
        assertEquals(BAD_REQUEST, e.getStatus());

        Optional<Map> mapOptional = e.getResponse().getBody(Map.class);
        assertTrue(mapOptional.isPresent());

        Map m = mapOptional.get();
        assertEquals("invalid_grant", m.get("error"));
        assertEquals("Refresh token is invalid", m.get("error_description"));
    }

    @Test
    void verifyJWTAccessTokenRefreshWorks() throws InterruptedException {
        String username = "alice";

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, "testPass");
        HttpRequest<?> request = HttpRequest.POST("/login", creds);

        long oldTokenCount = refreshTokenRepository.count();
        BearerAccessRefreshToken rsp = client.toBlocking().retrieve(request, BearerAccessRefreshToken.class);
        Thread.sleep(3_000);
        assertEquals(oldTokenCount + 1, refreshTokenRepository.count());

        assertNotNull(rsp.getAccessToken());
        assertNotNull(rsp.getRefreshToken());

        Thread.sleep(1_000);
        AccessRefreshToken refreshResponse = client.toBlocking().retrieve(HttpRequest.POST("/oauth/access_token",
                new TokenRefreshRequest(TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN, rsp.getRefreshToken())), AccessRefreshToken.class);

        assertNotNull(refreshResponse.getAccessToken());
        assertNotEquals(rsp.getAccessToken(), refreshResponse.getAccessToken());

        refreshTokenRepository.deleteAll();
    }
}
