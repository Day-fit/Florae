package pl.Dayfit.Florae.Services.Auth.API;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Helpers.SpEL.ApiKeysHelper;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyCacheService {
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApiKeysHelper helper;

    @CacheEvict(value = "api-keys", key = "@apiKeysHelper.generateShortKey(#rawApiKey)")
    public void revokeApiKey(String rawApiKey) throws IllegalArgumentException{
        String shortKey = helper.generateShortKey(rawApiKey);
        ApiKey apiKey = apiKeyRepository.findByShortKey(shortKey).stream().filter(entity -> passwordEncoder.matches(rawApiKey, entity.getKeyValue())).findFirst().orElse(null);

        if (apiKey == null)
        {
            throw new IllegalArgumentException("API key does not exist or is already revoked");
        }

        apiKey.setIsRevoked(true);
        apiKeyRepository.save(apiKey);
    }

    @Cacheable(value = "api-keys", key = "@apiKeysHelper.generateShortKey(#rawApiKey)")
    public ApiKey getApiKey(String rawApiKey)
    {
        String shortKey = helper.generateShortKey(rawApiKey);
        return apiKeyRepository.findAllByShortKey(shortKey).stream().filter(entity -> passwordEncoder.matches(rawApiKey, entity.getKeyValue())).findFirst().orElse(null);
    }

    @Cacheable(value = "api-keys", key = "#apiKeyValue")
    public ApiKey getApiKeyByHash(String apiKeyValue) {
        return apiKeyRepository.findByKeyValue(apiKeyValue);
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