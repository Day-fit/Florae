package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.Dayfit.Florae.Repositories.ApiKeyRepository;
import pl.Dayfit.Florae.Repositories.BlacklistJwtTokenRepository;

import java.util.Date;

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
