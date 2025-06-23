package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.GenerateApiKeyDTO;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Exceptions.AssociationException;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyService;
import pl.Dayfit.Florae.Services.PlantCacheService;

import java.util.Map;

/**
 * Controller class for managing API Key-related operations. This class provides endpoints
 * to generate, revoke, and validate API keys associated with a user.
 * <p>
 * Endpoints:
 * - Generate a new API Key
 * - Revoke an existing API Key
 * - Check the validity of an API Key
 * - Connect an API to the floralink device
 */
@RestController
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;
    private final PlantCacheService plantCacheService;

    @PostMapping("/api/v1/generate-key")
    public ResponseEntity<Map<String, String>> generateApiKey(@RequestBody GenerateApiKeyDTO apiKeyDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) throws AssociationException
    {
        if (apiKeyDTO == null)
        {
            throw new IllegalArgumentException("Incorrect request body");
        }

        Plant plant = plantCacheService.getPlantById(apiKeyDTO.getPlantId());

        if (plant == null)
        {
            throw new IllegalArgumentException("Invalid plant id");
        }

        return ResponseEntity.ok(Map.of("apiKey", apiKeyService.generateApiKey(userPrincipal.getUsername(), plant)));
    }

    @PostMapping("/api/v1/connect-api")
    public ResponseEntity<?> connectApi(Authentication authentication) throws AssociationException
    {
        apiKeyService.connectApi(authentication);
        return ResponseEntity.ok(Map.of("message", "API connected successfully."));
    }

    @DeleteMapping("/api/v1/revoke-key")
    public ResponseEntity<Map<String, String>> deleteApiKey(@RequestParam String apiKey, @AuthenticationPrincipal UserPrincipal userPrincipal) throws AssociationException
    {
        if (!apiKeyService.isValidApiKey(apiKey) && !apiKeyService.isOwner(apiKey, userPrincipal.getUsername())) // Avoid access denied to prevent exposing API key
        {
            throw new IllegalArgumentException("Invalid API key");
        }

        apiKeyService.revokeApiKey(apiKey);
        return ResponseEntity.ok(Map.of("message", "API key revoked successfully"));
    }

    @GetMapping("/api/v1/check-key")
    public ResponseEntity<Map<String, Object>> checkApiKey(@RequestParam String apiKey)
    {
        if (apiKey == null || apiKey.isBlank())
        {
           throw new IllegalArgumentException("API key cannot be empty or blank");
        }

        return ResponseEntity.ok(Map.of("result", apiKeyService.isValidApiKey(apiKey)));
    }
}
