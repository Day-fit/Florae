package pl.Dayfit.Florae.Services.Auth.API;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;
import pl.Dayfit.Florae.Repositories.JPA.FloraeUserRepository;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;
import pl.Dayfit.Florae.Services.FloraLinkCacheService;

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
    private final FloraeUserCacheService floraeUserCacheService;
    private final FloraLinkCacheService floraLinkCacheService;

    public String generateApiKey(String username) {
        String generatedUUID = UUID.randomUUID().toString();

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(generatedUUID);
        apiKey.setFloraeUser(floraeUserRepository.findByUsername(username));
        apiKeyRepository.save(apiKey);

        scheduler.schedule(cacheService::revokeUnusedApiKeys, 5, TimeUnit.MINUTES);

        return generatedUUID;
    }

    private String getOwner(String apiKeyValue) {
        ApiKey apiKey = getApiKey(apiKeyValue);
        return apiKey.getFloraeUser().getUsername();
    }

    public void revokeApiKey(String apiKeyValue) {
        cacheService.revokeApiKey(apiKeyValue);
    }

    public ApiKey getApiKey(String apiKeyValue) {
        return cacheService.getApiKey(apiKeyValue);
    }

    public boolean isOwner(String apiKeyValue, String username) {
        return username.equals(getOwner(apiKeyValue));
    }

    public boolean isValidApiKey(String apiKeyValue) {
        ApiKey apiKey = getApiKey(apiKeyValue);
        return apiKey != null && !apiKey.getIsRevoked();
    }

    public void connectApi(Authentication authentication) {
        ApiKey apiKey = (ApiKey) authentication.getCredentials();

        if (apiKey.getLinkedFloraLink() != null)
        {
            throw new IllegalStateException("This API key is already linked to a FloraLink");
        }

        FloraeUser floraeUser = floraeUserCacheService.getFloraeUserById(((FloraeUser) authentication.getPrincipal()).getId());

        FloraLink floraLink = new FloraLink();
        floraLink.setId(null);
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