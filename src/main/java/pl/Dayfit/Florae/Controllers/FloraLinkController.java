package pl.Dayfit.Florae.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorDataDTO;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyService;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;
import pl.Dayfit.Florae.Services.FloraLinkCacheService;
import pl.Dayfit.Florae.Services.FloraLinkService;

/**
 * Controller for handling requests related to the FloraLink API, including operations for
 * connecting APIs, uploading sensor data, and retrieving sensor data reports.
 */
@RestController
@RequiredArgsConstructor
public class FloraLinkController {
    private final FloraLinkService floraLinkService;
    private final ApiKeyService apiKeyService;
    private final FloraLinkCacheService floraLinkCacheService;
    private final FloraeUserCacheService floraeUserCacheService;

    @PostMapping("/api/v1/floralink/connect-api")
    public ResponseEntity<?> connectApi(Authentication authentication)
    {
        try{
            apiKeyService.connectApi(authentication);
        } catch (IllegalStateException exception)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "API connected successfully."));
    }

    @PostMapping("/api/v1/floralink/upload-daily-report")
    public ResponseEntity<?> uploadReport(@RequestBody @Valid List<DailySensorDataDTO> uploadedData, Authentication authentication)
    {
        floraLinkService.handleReportUpload(uploadedData, authentication);
        return ResponseEntity.ok(Map.of("message", "Report uploaded successfully."));
    }

    @GetMapping("/api/v1/floralink/get-all-daily-data")
    public ResponseEntity<?> getAllData(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        return ResponseEntity.ok(floraLinkService.getDailyDataReport((userPrincipal).getUsername()));
    }

    @PostMapping("/api/v1/floralink/upload-current-data")
    public ResponseEntity<?> uploadCurrentData(@RequestBody @Valid List<CurrentSensorDataDTO> uploadedData, Authentication authentication)
    {
        floraLinkService.handleCurrentDataUpload(uploadedData, authentication);
        return ResponseEntity.ok(Map.of("message", "Data uploaded successfully."));
    }

    @GetMapping("/api/v1/floralink/get-all-current-data")
    public ResponseEntity<?> getAllCurrentData(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        return ResponseEntity.ok(floraLinkService.getCurrentDataReport((userPrincipal).getUsername()));
    }

    @GetMapping("/api/v1/get-floralinks")
    public ResponseEntity<?> getLinkedFloraLinks(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(floraLinkCacheService.getOwnedFloraLinks(floraeUserCacheService.getFloraeUser(userPrincipal.getUsername()).getId()));
    }
}