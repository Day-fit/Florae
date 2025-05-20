package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FloraLinkController {
    @PostMapping("/api/v1/esp/upload-data")
    public ResponseEntity<?> uploadData(@RequestBody String apiKeyValue)
    {
        //To be implemented in the future.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("This endpoint is not implemented yet.");
    }

    @PostMapping("/api/v1/esp/get-data")
    public ResponseEntity<?> getData(@RequestBody String apiKeyValue)
    {
        //To be implemented in the future.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("This endpoint is not implemented yet.");
    }
}
