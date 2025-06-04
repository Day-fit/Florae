package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyService;

import java.util.Map;



/**
 * Controller class for managing API Key-related operations. This class provides endpoints
 * to generate, revoke, and validate API keys associated with a user.
 * <p>
 * Endpoints:
 * - Generate a new API Key
 * - Revoke an existing API Key
 * - Check the validity of an API Key
 */
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

        try {
            apiKeyService.revokeApiKey(apiKey);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "API key is invalid or has been already revoked."));
        }

        return ResponseEntity.ok(Map.of("message", "API key revoked successfully"));
    }

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
