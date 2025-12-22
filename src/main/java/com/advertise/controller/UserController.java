package com.advertise.controller;

import com.advertise.dto.register.RegisterRequest;
import com.advertise.dto.register.RegisterResponse;
import com.advertise.entity.User;
import com.advertise.exception.DefaultErrorCodes;
import com.advertise.exception.ErrorResponse;
import com.advertise.exception.UserAlreadyExistsException;
import com.advertise.security.AuthenticationProviderService;
import com.advertise.service.RegisterService;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Controller
public class UserController {

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final RegisterService registerService;
    private final AuthenticationProviderService authenticationProviderService;
    private final AccessRefreshTokenGenerator tokenGenerator;

    @Inject
    public UserController(
            RegisterService registerService,
            AuthenticationProviderService authenticationProviderService,
            AccessRefreshTokenGenerator tokenGenerator) {
        this.registerService = registerService;
        this.authenticationProviderService = authenticationProviderService;
        this.tokenGenerator = tokenGenerator;
    }

    @Post("/login")
    @Consumes({"application/json", "application/x-www-form-urlencoded"})
    @Secured(SecurityRule.IS_ANONYMOUS)
    @SingleResult
    public Publisher<MutableHttpResponse<?>> login(@Body UsernamePasswordCredentials creds, HttpRequest<?> request) {
        return Mono.from(authenticationProviderService.authenticate(request, creds))
                .map(authResponse -> {
                    if (!(authResponse instanceof AuthenticationResponse auth) ||
                            !auth.isAuthenticated() ||
                            auth.getAuthentication().isEmpty()) {
                        return HttpResponse.unauthorized();
                    }

                    BearerAccessRefreshToken tokens = (BearerAccessRefreshToken)
                            tokenGenerator.generate(auth.getAuthentication().get()).orElseThrow();

                    MutableHttpResponse<Map<String, String>> response = HttpResponse.ok(
                            Map.of("message", "Login successful", "username", tokens.getUsername())
                    );

                    response.cookie(createCookie("access_token", tokens.getAccessToken(), ACCESS_TOKEN_TTL));
                    response.cookie(createCookie("refresh_token", tokens.getRefreshToken(), REFRESH_TOKEN_TTL));

                    return response;
                });
    }

    @Post("/register")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<?> register(@Valid @Body RegisterRequest request) {
        try {
            User user = registerService.register(request);
            return HttpResponse.created(new RegisterResponse(
                    user.name(),
                    user.email(),
                    "User registered successfully. Please login."
            ));
        } catch (UserAlreadyExistsException e) {
            return HttpResponse.badRequest(new ErrorResponse(DefaultErrorCodes.METHOD_NOT_ALLOWED, e.getMessage()));
        } catch (Exception e) {
            return HttpResponse.serverError(new ErrorResponse(DefaultErrorCodes.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    private Cookie createCookie(String name, String value, Duration maxAge) {
        return Cookie.of(name, value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .sameSite(SameSite.Strict);
    }

    private Cookie expireCookie(String name) {
        return Cookie.of(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ZERO);
    }
}