package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Repositories.ApiKeyRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableAsync
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;

    public String generateApiKey(String username)
    {
        String generatedUUID = UUID.randomUUID().toString();

        ApiKey apiKey = new ApiKey();
        apiKey.setValue(generatedUUID);
        apiKey.setCreatedBy(username);
        apiKeyRepository.save(apiKey);

        return generatedUUID;
    }

    private String getOwner(String apiKeyValue)
    {
        ApiKey apiKey = apiKeyRepository.getApiKeysByValue(apiKeyValue);
        return apiKey.getCreatedBy();
    }

    public boolean isOwner(String apiKeyValue, String username)
    {
        return username.equals(getOwner(apiKeyValue));
    }

    public boolean isValidApiKey(String apiKeyValue)
    {
        ApiKey apiKey = apiKeyRepository.getApiKeysByValue(apiKeyValue);
        return apiKey != null && !apiKey.getIsRevoked();
    }

    public void revokeApiKey(String apiKeyValue) {
        ApiKey apiKey = apiKeyRepository.getApiKeysByValue(apiKeyValue);
        apiKey.setIsRevoked(true);
        apiKeyRepository.save(apiKey);
    }
}
