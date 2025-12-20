package com.advertise.controller;

import com.advertise.dto.register.RegisterRequest;
import com.advertise.dto.register.RegisterResponse;
import com.advertise.entity.User;
import com.advertise.exception.ErrorResponse;
import com.advertise.exception.UserAlreadyExistsException;
import com.advertise.security.TokenPersistence;
import com.advertise.service.RegisterService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.Map;

import static com.advertise.exception.DefaultErrorCodes.INTERNAL_SERVER_ERROR;
import static com.advertise.exception.DefaultErrorCodes.METHOD_NOT_ALLOWED;

@Controller
@Validated
public class UserController {

    private final RegisterService registerService;
    private final TokenPersistence tokenPersistence;

    public UserController(RegisterService registerService, TokenPersistence tokenPersistence) {
        this.registerService = registerService;
        this.tokenPersistence = tokenPersistence;
    }

    @Post("/register")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<?> register(@Valid @Body RegisterRequest registerRequest) {
        try {
            User user = registerService.register(registerRequest);

            RegisterResponse response = new RegisterResponse(
                    user.name(),
                    user.email(),
                    "User registered successfully. Please login."
            );

            return HttpResponse.created(response);
        } catch (UserAlreadyExistsException e) {
            return HttpResponse.badRequest(new ErrorResponse(METHOD_NOT_ALLOWED, e.getMessage()));
        } catch (Exception e) {
            return HttpResponse.serverError(new ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @Post("/logout")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public HttpResponse<?> logout(Principal principal) {
        tokenPersistence.revokeAllForUser(principal.getName());

        return HttpResponse.ok(Map.of(
                "message", "Logged out successfully. Please discard your tokens."
        ));
    }
}
