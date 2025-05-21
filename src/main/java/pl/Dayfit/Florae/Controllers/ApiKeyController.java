package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.Services.ApiKeyService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @CachePut(value = "api-keys", key = "#userPrincipal.username")
    @PostMapping("/api/v1/generate-key")
    public ResponseEntity<Map<String, String>> generateApiKey(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        return ResponseEntity.ok(Map.of("apiKey", apiKeyService.generateApiKey(userPrincipal.getUsername())));
    }

    @DeleteMapping("/api/v1/revoke-key")
    public ResponseEntity<Map<String, String>> deleteApiKey(@RequestParam String apiKey, @AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        if (!apiKeyService.isValidApiKey(apiKey) && !apiKeyService.isOwner(apiKey, userPrincipal.getUsername()))
        {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid api key"));
        }

        apiKeyService.revokeApiKey(apiKey);

        return ResponseEntity.ok(Map.of("message", "API key revoked successfully"));
    }

    //TODO: add too many requests error to avoid brute force attacks
    @GetMapping("/api/v1/check-key")
    public ResponseEntity<Map<String, Object>> checkApiKey(@RequestParam String apiKey)
    {
        if (apiKey == null || apiKey.isBlank())
        {
            return ResponseEntity.badRequest().body(Map.of("error", "API key cannot be empty or blank"));
        }

        return ResponseEntity.ok(Map.of("result", apiKeyService.isValidApiKey(apiKey)));
    }
}
