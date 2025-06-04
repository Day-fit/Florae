package pl.Dayfit.Florae.Services.Auth.API;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationCandidate;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Helpers.SpEL.ApiKeysHelper;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;
import pl.Dayfit.Florae.Repositories.JPA.FloraeUserRepository;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;
import pl.Dayfit.Florae.Services.FloraLinkCacheService;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service class responsible for managing API key functionality.
 * Provides operations for generating, revoking, validating, and
 * associating API keys with users and related resources.
 * <p>
 * Annotations:
 * - {@code @Service}: Marks this class as a Spring Service component.
 * - {@code @RequiredArgsConstructor}: Generates a constructor for all final fields.
 * - {@code @EnableAsync}: Enables asynchronous method execution.
 * - {@code @Slf4j}: Provides logging capability using SLF4J.
 * <p>
 * Key Responsibilities:
 * 1. Generate and persist API keys for specific users.
 * 2. Validate the ownership and validity of an API key.
 * 3. Revoke API keys when necessary to ensure security.
 * 4. Manage associations between API keys and related resources.
 * 5. Leverage caching mechanisms to improve performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableAsync
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final FloraeUserRepository floraeUserRepository;
    private final ApiKeyCacheService cacheService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final FloraeUserCacheService floraeUserCacheService;
    private final FloraLinkCacheService floraLinkCacheService;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeysHelper apiKeyHelper;
    private final ApiKeyCacheService apiKeyCacheService;

    @Transactional
    public String generateApiKey(String username, Plant plant) throws IllegalArgumentException {
        String generatedUUID = UUID.randomUUID().toString();
        String encryptedUUID = passwordEncoder.encode(generatedUUID);

        if (!plant.getLinkedUser().getUsername().equals(username))
        {
            throw new IllegalArgumentException("Plant is not owned by user");
        }

        if (plant.getLinkedFloraLink() != null)
        {
            throw new IllegalArgumentException("Plant is already linked to a FloraLink");
        }

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(encryptedUUID);
        apiKey.setLinkedPlant(plant);
        apiKey.setShortKey(apiKeyHelper.generateShortKey(generatedUUID));
        apiKey.setFloraeUser(floraeUserRepository.findByUsername(username));
        apiKeyRepository.save(apiKey);

        scheduler.schedule(cacheService::revokeUnusedApiKeys, 5, TimeUnit.MINUTES);

        return generatedUUID;
    }

    private String getOwner(String apiKeyValue) {
        ApiKey apiKey = getApiKey(apiKeyValue);
        return apiKey.getFloraeUser().getUsername();
    }

    public void revokeApiKey(String apiKeyValue) throws IllegalArgumentException{
        cacheService.revokeApiKey(apiKeyValue);
    }

    public ApiKey getApiKey(String apiKeyValue) {
        return cacheService.getApiKey(apiKeyValue);
    }

    public ApiKey getApiKeyByHash(String hash)
    {
        return cacheService.getApiKeyByHash(hash);
    }

    public boolean isOwner(String apiKeyValue, String username) {
        return username.equals(getOwner(apiKeyValue));
    }

    public boolean isValidApiKey(String apiKeyValue) {
        ApiKey apiKey = getApiKey(apiKeyValue);
        return apiKey != null && !apiKey.getIsRevoked();
    }

    public boolean isValidByAuthentication(Authentication authentication) {
        if (!(authentication instanceof ApiKeyAuthenticationCandidate))
        {
            return false;
        }

        ApiKey apiKey = (ApiKey) authentication.getCredentials();
        return apiKey != null && !apiKey.getIsRevoked();
    }

    @Transactional
    public void connectApi(Authentication authentication) {
        ApiKey apiKey = apiKeyCacheService.getApiKeyByHash(((ApiKey) authentication.getCredentials()).getKeyValue());

        if (apiKey.getLinkedFloraLink() != null)
        {
            throw new IllegalStateException("This API key is already linked to a FloraLink");
        }

        FloraeUser floraeUser = floraeUserCacheService.getFloraeUserById(((FloraeUser) authentication.getPrincipal()).getId());

        FloraLink floraLink = new FloraLink();
        floraLink.setId(null);
        floraLink.setLinkedPlant(apiKey.getLinkedPlant());
        floraLink.setName("FloraLink");
        floraLink.setOwner(floraeUser);

        apiKey.setLinkedFloraLink(floraLinkCacheService.saveFloraLink(floraLink));

        apiKeyRepository.save(apiKey);
    }

    private boolean hasLinkedUser(ApiKey apiKey) {
        return apiKey.getLinkedFloraLink() != null;
    }

    private boolean hasLinkedFloraLink(ApiKey apiKey) {
        return apiKey.getLinkedFloraLink().getId() != null;
    }

    public boolean isLinked(String apiKeyValue) {
        ApiKey apiKey = getApiKey(apiKeyValue);
        return hasLinkedUser(apiKey) && hasLinkedFloraLink(apiKey);
    }
}