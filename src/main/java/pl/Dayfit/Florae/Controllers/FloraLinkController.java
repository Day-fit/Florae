package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.FloraLinkSetNameDTO;
import pl.Dayfit.Florae.DTOs.Sensors.EnableBleDTO;
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

    @PostMapping("/api/v1/floralink/enable-ble")
    public ResponseEntity<Map<String, String>> sendRequestToEnableBle(@RequestBody EnableBleDTO dto, @AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        Integer floralinkId = dto.getFloraLinkId();

        if (floralinkId == null)
        {
            throw new IllegalArgumentException("Incorrect request body");
        }

        floraLinkService.handleEnablingBle(floralinkId, userPrincipal.getUsername());
        return ResponseEntity.ok(Map.of("message", "Request to enable BLE has been sent to the floralink device."));
    }

    @GetMapping("/api/v1/floralink/get-all-daily-data")
    public ResponseEntity<?> getAllData(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        return ResponseEntity.ok(floraLinkService.getDailyDataReport((userPrincipal).getUsername()));
    }

    @PutMapping("/api/v1/floralink/set-name")
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