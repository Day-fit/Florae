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
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Services.PlantCacheService;
import pl.Dayfit.Florae.Services.PlantsService;

/**
 * Controller for handling operations related to plants. It provides endpoints
 * for uploading plant photos for recognition, setting plant name, deleting plant and
 * fetching plants associated with a user's account.
 */
@RestController
@RequiredArgsConstructor
class PlantsController {
    private final PlantsService plantsService;
    private final PlantCacheService plantCacheService;

    @PostMapping("/api/v1/add-plant")
    public Callable<ResponseEntity<?>> addPlant(@RequestParam List<MultipartFile> photos, @AuthenticationPrincipal UserPrincipal user)
    {
        if (photos == null || photos.isEmpty()) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No photos provided"));
        }

        return () -> {
            try {
                Plant savedPlant = plantsService.saveAndRecognise(photos, user.getUsername());
                return ResponseEntity.ok(Map.of("id", savedPlant.getId(), "speciesName", savedPlant.getSpeciesName()));
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
        if (plantSetNameDTO == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Incorrect request body"));
        }

        if (plantSetNameDTO.getPlantId() == null || plantSetNameDTO.getName() == null || plantSetNameDTO.getName().isBlank())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Id or name is missing"));
        }

        if (plantCacheService.getPlantById(plantSetNameDTO.getPlantId()) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No plants found"));
        }

        if (plantsService.isNotOwner(plantSetNameDTO.getPlantId(), user.getUsername()))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not owner of this plant"));
        }

        plantsService.saveName(plantSetNameDTO.getPlantId(), plantSetNameDTO.getName());
        return ResponseEntity.ok(Map.of("message", "Successfully set plant name"));
    }

    @DeleteMapping("/api/v1/delete-plant/{plantId:\\d+}")
    public ResponseEntity<?> deletePlant(@PathVariable Integer plantId, @AuthenticationPrincipal UserPrincipal user)
    {
        if (plantId == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Id is missing"));
        }

        if (plantCacheService.getPlantById(plantId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No plant found"));
        }

        if (plantsService.isNotOwner(plantId, user.getUsername()))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not owner of this plant"));
        }

        plantCacheService.deletePlant(plantId);
        return ResponseEntity.ok(Map.of("message", "Successfully deleted plant"));
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