package com.advertise.security;

import com.advertise.entity.RefreshToken;
import com.advertise.repository.RefreshTokenRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Instant;
import java.util.Optional;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT;

@Singleton
public class TokenPersistence implements RefreshTokenPersistence {

    private final RefreshTokenRepository repository;

    public TokenPersistence(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void persistToken(RefreshTokenGeneratedEvent event) {
        if (event == null || event.getRefreshToken() == null ||
                event.getAuthentication() == null || event.getAuthentication().getName() == null) {
            return;
        }

        String username = event.getAuthentication().getName();
        String reference = event.getRefreshToken();

        int updated = repository.updateByUsername(username, reference);

        if (updated == 0) {
            repository.save(new RefreshToken(
                    null,
                    username,
                    reference,
                    false,
                    Instant.now()
            ));
        }
    }

    @Override
    public Publisher<Authentication> getAuthentication(String jwt) {
        return Flux.create(emitter -> {
            try {
                Optional<RefreshToken> tokenOpt = repository.findByRefreshToken(jwt);

                if (tokenOpt.isEmpty()) {
                    emitter.error(new OauthErrorResponseException(INVALID_GRANT, "refresh token not found", null));
                    return;
                }

                RefreshToken token = tokenOpt.get();

                if (token.revoked()) {
                    emitter.error(new OauthErrorResponseException(INVALID_GRANT, "refresh token revoked", null));
                    return;
                }

                emitter.next(Authentication.build(token.username()));
                emitter.complete();

            } catch (IllegalArgumentException e) {
                emitter.error(new OauthErrorResponseException(INVALID_GRANT, "invalid refresh token", null));
            }
        }, FluxSink.OverflowStrategy.ERROR);
    }
}
