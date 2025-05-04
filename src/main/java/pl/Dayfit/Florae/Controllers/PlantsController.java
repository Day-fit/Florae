package pl.Dayfit.Florae.Controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Services.PlantsService;

@RestController
@RequiredArgsConstructor
class PlantsController {
    private final PlantsService plantsService;

    @PostMapping("/api/v1/upload-photos")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam ArrayList<MultipartFile> photos, @AuthenticationPrincipal UserPrincipal user)
    {


        if (photos == null || photos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No photos provided"));
        }
        
        try{
            return ResponseEntity.ok(Map.of("speciesName", plantsService.saveAndRecognise(photos, user.getUsername())));
        } catch(IOException exception){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed at uploading photos"));
        } catch (NoSuchElementException | IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No matches or requirements found"));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid photo format"));
        }
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