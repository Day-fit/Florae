package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;
import pl.Dayfit.Florae.Repositories.JPA.FloraeUserRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableAsync
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final FloraeUserRepository floraeUserRepository;
    private final ApiKeyCacheService cacheService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public String generateApiKey(String username)
    {
        String generatedUUID = UUID.randomUUID().toString();

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(generatedUUID);
        apiKey.setFloraeUser(floraeUserRepository.findByUsername(username));
        apiKeyRepository.save(apiKey);

        scheduler.schedule(this::revokeUnusedApiKeys, 5, TimeUnit.MINUTES);

        return generatedUUID;
    }

    private String getOwner(String apiKeyValue)
    {
        ApiKey apiKey = getApiKey(apiKeyValue);
        return apiKey.getLinkedFloraLink().getOwner().getUsername();
    }

    public void revokeApiKey(String apiKeyValue) {
        cacheService.revokeApiKey(apiKeyValue);
    }

    public ApiKey getApiKey(String apiKeyValue)
    {
        return cacheService.getApiKey(apiKeyValue);
    }

    public boolean isOwner(String apiKeyValue, String username)
    {
        return username.equals(getOwner(apiKeyValue));
    }

    public boolean isValidApiKey(String apiKeyValue)
    {
        ApiKey apiKey = getApiKey(apiKeyValue);
        return apiKey != null && !apiKey.getIsRevoked();
    }

    private void revokeUnusedApiKeys()
    {
        apiKeyRepository.findUnusedApiKeysBeforeDate(Instant.now()).forEach(apiKey ->
        {
            apiKey.setIsRevoked(true);
            apiKeyRepository.save(apiKey);
        });
    }
}
