package pl.Dayfit.Florae.Controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pl.Dayfit.Florae.Services.PlantsService;

@RestController
@RequiredArgsConstructor
class PlantsController {
    private final PlantsService filesService;

    @PostMapping("/api/v1/upload-photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam MultipartFile file)
    {
        try{
            filesService.saveToServer(file);
        } catch(IOException exception){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Upload failed !")); 
        }

        return ResponseEntity.ok(Map.of("message", "Uploaded successfully"));
    }

    @PostMapping("/api/v1/upload-diagnosis")
    public ResponseEntity<Map<String, String>> uploadDiagnosis(@RequestParam MultipartFile file)
    {


        return ResponseEntity.ok(Map.of("message", "Uploaded successfully"));
    }
}