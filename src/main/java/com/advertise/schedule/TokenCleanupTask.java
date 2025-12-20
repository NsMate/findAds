package com.advertise.schedule;

import com.advertise.repository.RefreshTokenRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Singleton
public class TokenCleanupTask {

    private static final Logger LOG = LoggerFactory.getLogger(TokenCleanupTask.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupTask(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldTokens() {
        LOG.info("Starting cleanup of old refresh tokens");

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        long deletedCount = refreshTokenRepository.deleteByDateCreatedBefore(oneDayAgo);

        LOG.info("Deleted {} old refresh tokens", deletedCount);
    }
}
