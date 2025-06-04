package pl.Dayfit.Florae.Controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.PlantResponseDTO;
import pl.Dayfit.Florae.DTOs.PlantSetNameDTO;
import pl.Dayfit.Florae.Services.PlantsService;

/**
 * Controller for handling operations related to plants. It provides endpoints
 * for uploading plant photos for recognition, retrieving plant data by ID, and
 * fetching plants associated with a user's account.
 */
@RestController
@RequiredArgsConstructor
class PlantsController {
    private final PlantsService plantsService;

    @PostMapping("/api/v1/add-plant")
    public Callable<ResponseEntity<Map<String, String>>> addPlant(@RequestParam List<MultipartFile> photos, @AuthenticationPrincipal UserPrincipal user)
    {
        if (photos == null || photos.isEmpty()) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No photos provided"));
        }


        return () -> {
            try {
                return ResponseEntity.ok(Map.of("speciesName", plantsService.saveAndRecognise(photos, user.getUsername())));
            } catch(IOException exception) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed at uploading photos"));
            } catch (NoSuchElementException | IllegalStateException exception) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No matches or requirements found"));
            } catch (IllegalArgumentException exception) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid photo format"));
            }
        };
    }

    @PostMapping("/api/v1/plant-set-name")
    public ResponseEntity<?> setPlantName(@RequestBody PlantSetNameDTO plantSetNameDTO, @AuthenticationPrincipal UserPrincipal user)
    {
        if (plantSetNameDTO == null) {}
    }

    @GetMapping("/api/v1/plant/{id:\\d+}")
    public ResponseEntity<?> getPlantById(@PathVariable Integer id)
    {
        if(id == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Invalid id"));
        }

        PlantResponseDTO response = plantsService.getPlantById(id);

        if(response == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Plant not found"));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/plants")
    public ResponseEntity<?> getOwnedPlants(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        String username = userPrincipal.getUsername();

        if(username == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User is not logged in"));
        }

        List<PlantResponseDTO> plants = plantsService.getPlantsByUsername(username);

        if (plants == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No plants found"));
        }

        return ResponseEntity.ok(plants);
    }
}