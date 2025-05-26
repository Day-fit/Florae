package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;
import pl.Dayfit.Florae.Repositories.JPA.BlacklistJwtTokenRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Service responsible for periodic maintenance operations, focusing on
 * removing unused or invalid data from the database.
 * <p>
 * This service leverages Spring's {@code @Scheduled} functionality to
 * execute maintenance tasks at a fixed interval defined by the constant
 * {@code MAINTENANCE_PERIOD}.
 * <p>
 * Tasks performed:
 * - Removal of revoked API keys from the system to free up resources and
 *   maintain data integrity.
 * - Deletion of expired blacklisted JSON Web Tokens (JWTs) to ensure
 *   outdated tokens are no longer tracked.
 * <p>
 * Annotations:
 * - {@code @Service}: Marks this class as a Spring-managed service component.
 * - {@code @RequiredArgsConstructor}: Automatically generates a constructor
 *   for injecting required dependencies.
 * - {@code @Scheduled}: Indicates periodic execution of methods based on fixed
 *   timing configurations.
 * - {@code @Transactional}: Ensures the execution of database operations within
 *   a transactional context.
 * - {@code @Slf4j}: Provides the class with logging capabilities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final ApiKeyRepository apiKeyRepository;
    private final BlacklistJwtTokenRepository blacklistJwtTokenRepository;
    public static final int MAINTENANCE_PERIOD = 1000 * 60 * 60;

    @Transactional
    @Scheduled(fixedRate = MAINTENANCE_PERIOD)
    public void removeRevokedApiKeys()
    {
        log.info("Removing revoked API keys...");
        int deletedCount = apiKeyRepository.deleteRevokedApiKeys();
        log.info("Removed {} revoked API keys", deletedCount);
    }

    @Transactional
    @Scheduled(fixedRate = MAINTENANCE_PERIOD)
    public void removeExpiredTokens()
    {
        log.info("Removing expired JWT tokens...");
        int deletedCount = blacklistJwtTokenRepository.deleteAllExpiredTokens(new Date());
        log.info("Removed {} expired blacklisted JWT tokens", deletedCount);
    }
}
