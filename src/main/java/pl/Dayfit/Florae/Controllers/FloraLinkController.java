package pl.Dayfit.Florae.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.FloraLinkSetNameDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorDataDTO;
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
    private final FloraLinkCacheService floraLinkCacheService;
    private final FloraeUserCacheService floraeUserCacheService;

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

    @PostMapping("/api/v1/floralink/set-name")
    public ResponseEntity<?> setFloraLinkName(@RequestBody FloraLinkSetNameDTO floraLinkSetNameDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) throws AccessDeniedException
    {
        if (floraLinkSetNameDTO == null || floraLinkSetNameDTO.getId() == null || floraLinkSetNameDTO.getName() == null || floraLinkSetNameDTO.getName().isBlank())
        {
            throw new IllegalArgumentException("Incorrect request body");
        }

        floraLinkService.setName(floraLinkSetNameDTO, userPrincipal.getUsername());

        return ResponseEntity.ok(Map.of("message", "Name set successfully."));
    }

    @GetMapping("/api/v1/get-floralinks")
    public ResponseEntity<?> getLinkedFloraLinks(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(floraLinkCacheService.getOwnedFloraLinks(floraeUserCacheService.getFloraeUser(userPrincipal.getUsername()).getId()));
    }
}