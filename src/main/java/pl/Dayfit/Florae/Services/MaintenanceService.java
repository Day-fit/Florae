package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Repositories.ApiKeyRepository;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final ApiKeyRepository apiKeyRepository;
    public final static int API_KEY_EXPIRATION_TIME = 1000 * 60 * 5;

    @Scheduled(fixedDelay = API_KEY_EXPIRATION_TIME)
    public void expireApiKeys()
    {
        List<ApiKey> expiredApiKeys = apiKeyRepository.findUnusedApiKeysBeforeDate(Instant.now());
        expiredApiKeys.forEach(apiKey -> {
            apiKey.setIsRevoked(true);
            apiKeyRepository.save(apiKey);
            log.info("API Key {} has been revoked due to inactivity.", apiKey.getValue());
        });
    }
}
