package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;

@Service
@RequiredArgsConstructor
public class ApiKeyCacheService {
    private final ApiKeyRepository apiKeyRepository;

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
}
