package pl.Dayfit.Florae.Services.Auth.API;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ApiKeyCacheService {
    private final ApiKeyRepository apiKeyRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "api-keys", key = "#apiKeyValue")
    public ApiKey getApiKey(String apiKeyValue)
    {
        return apiKeyRepository.findAllByKeyValue(apiKeyValue);
    }

    @CachePut(value = "api-keys", key = "#apiKeyValue")
    public void revokeApiKey(String apiKeyValue) {
        ApiKey apiKey = apiKeyRepository.findAllByKeyValue(apiKeyValue);
        apiKey.setIsRevoked(true);
        apiKeyRepository.save(apiKey);
    }

    public void revokeUnusedApiKeys()
    {
        apiKeyRepository.findUnusedApiKeysBeforeDate(Instant.now()).forEach(apiKey ->
        {
            apiKey.setIsRevoked(true);
            redisTemplate.delete(apiKey.getKeyValue());
            apiKeyRepository.save(apiKey);
        });
    }
}
