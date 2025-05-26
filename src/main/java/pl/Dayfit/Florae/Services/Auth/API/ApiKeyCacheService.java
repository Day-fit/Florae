package pl.Dayfit.Florae.Services.Auth.API;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;

import java.time.Instant;

/**
 * Service class responsible for managing and caching API keys in the application.
 * It integrates with a database repository and a Redis cache to facilitate
 * retrieval, revocation, and cleanup operations for API keys.
 * <p>
 * Dependencies:
 * - ApiKeyRepository: Provides access to the database for CRUD operations on API key entities.
 * - RedisTemplate: Used for interacting with the Redis key-value store to manage cached API key data.
 * <p>
 * Annotations:
 * - {@code @Service}: Marks this class as a Spring service component.
 * - {@code @RequiredArgsConstructor}: Generates a constructor for required dependencies.
 */
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
