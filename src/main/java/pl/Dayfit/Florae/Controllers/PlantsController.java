package pl.Dayfit.Florae.Controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Services.PlantsService;

@RestController
@RequiredArgsConstructor
class PlantsController {
    private final PlantsService plantsService;

    @PostMapping("/api/v1/upload-photos")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam ArrayList<MultipartFile> photos)
    {
        try{
            if(!plantsService.saveAndRecognise(photos))
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No matches found"));
            }
        } catch(IOException exception){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Upload failed"));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No matches found"));
        }

        return ResponseEntity.ok(Map.of("message", "Uploaded successfully"));
    }

    @GetMapping("/api/v1/plant/{id}")
    public ResponseEntity<Plant> getPlant(@PathVariable Integer id)
    {
        Plant response = plantsService.getPlantById(id);

        if(response == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(response);
    }
}