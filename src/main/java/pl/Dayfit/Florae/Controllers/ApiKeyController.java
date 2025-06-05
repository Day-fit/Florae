package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.GenerateApiKeyDTO;
import pl.Dayfit.Florae.Entities.Plant;
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
 */
@RestController
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;
    private final PlantCacheService plantCacheService;

    @PostMapping("/api/v1/generate-key")
    public ResponseEntity<Map<String, String>> generateApiKey(@RequestBody GenerateApiKeyDTO apiKeyDTO, @AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        Plant plant = plantCacheService.getPlantById(apiKeyDTO.getPlantId());

        if (plant == null)
        {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid plant id"));
        }

        try {
            return ResponseEntity.ok(Map.of("apiKey", apiKeyService.generateApiKey(userPrincipal.getUsername(), plant)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", exception.getMessage()));
        } catch (DataIntegrityViolationException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "This plant has already been associated with an API key."));
        }
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
