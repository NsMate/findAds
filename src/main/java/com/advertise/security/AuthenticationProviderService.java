package com.advertise.security;

import com.advertise.entity.User;
import com.advertise.repository.UserRepository;
import com.advertise.security.encoder.PasswordEncoder;
import io.micronaut.http.HttpRequest;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestReactiveAuthenticationProvider;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Singleton
public class AuthenticationProviderService<B> implements HttpRequestReactiveAuthenticationProvider<B> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Scheduler scheduler;

    public AuthenticationProviderService(UserRepository userRepository,
                                         PasswordEncoder passwordEncoder,
                                         @Named(TaskExecutors.BLOCKING) ExecutorService executorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.scheduler = Schedulers.fromExecutorService(executorService);
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            HttpRequest<B> request,
            AuthenticationRequest<String, String> authRequest) {

        String username = authRequest.getIdentity();
        String password = authRequest.getSecret();

        return Flux.<AuthenticationResponse>create(emitter -> {
            Optional<User> userOpt = userRepository.findByNameEquals(username);

            if (userOpt.isEmpty()) {
                passwordEncoder.matches(password, "$2a$10$dummyHashToPreventTimingAttacks");
                emitter.error(new AuthenticationException(
                        new AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH)));
                return;
            }

            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.passwordHash())) {
                emitter.next(AuthenticationResponse.success(user.name()));
                emitter.complete();
            } else {
                emitter.error(new AuthenticationException(
                        new AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH)));
            }
        }, FluxSink.OverflowStrategy.ERROR).subscribeOn(scheduler);
    }
}
