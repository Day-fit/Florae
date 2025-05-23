package pl.Dayfit.Florae.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationToken;
import pl.Dayfit.Florae.DTOs.Sensors.ReportDataDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Services.FloraLinkService;

@RestController
@RequiredArgsConstructor
public class FloraLinkController {
    private final FloraLinkService floraLinkService;

    @PostMapping("/api/v1/floralink/upload-report")
    public ResponseEntity<?> uploadReport(@RequestBody @Valid ReportDataDTO uploadedData)
    {
        floraLinkService.handleReportUpload(uploadedData);
        return ResponseEntity.ok(Map.of("message", "Data uploaded successfully."));
    }

    @GetMapping("/api/v1/floralink/get-all-data")
    public ResponseEntity<?> getAllData(@AuthenticationPrincipal ApiKeyAuthenticationToken apiKeyAuthenticationToken)
    {
        Object principal = apiKeyAuthenticationToken.getPrincipal();

        if (!(principal instanceof FloraeUser))
        {
            return ResponseEntity.status(401).body("Unauthorized access");
        }

        return ResponseEntity.ok(floraLinkService.getAllReportData(((FloraeUser) principal).getUsername()));
    }

    @PostMapping("/api/v1/floralink/upload-current-data")
    public ResponseEntity<?> uploadCurrentData()
    {
        
        return null;
    }
}
