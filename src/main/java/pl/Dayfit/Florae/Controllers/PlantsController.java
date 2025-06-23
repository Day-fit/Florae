package pl.Dayfit.Florae.Controllers;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.Plants.PlantResponseDTO;
import pl.Dayfit.Florae.DTOs.Plants.PlantSetNameDTO;
import pl.Dayfit.Florae.DTOs.Plants.PlantSetVolumeDTO;
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
            return () -> { throw new IllegalArgumentException("No photos provided"); };
        }

        return () -> {
            Plant savedPlant = plantsService.saveAndRecognise(photos, user.getUsername());
            return ResponseEntity.ok(Map.of("id", savedPlant.getId(), "speciesName", savedPlant.getSpeciesName()));
        };
    }

    /**
     * Set the plant's pot volume
     * @param plantSetVolumeDTO DTO that contains plant id and new volume in liters
     * @param user User's authentication principal
     * @return Response entity with adequate response code
     */
    @PutMapping("/api/v1/set-pot-volume")
    public ResponseEntity<Map<String, String>> setPotVolume(@RequestBody PlantSetVolumeDTO plantSetVolumeDTO, @AuthenticationPrincipal UserPrincipal user)
    {
        if (plantSetVolumeDTO == null)
        {
            throw new IllegalArgumentException("Incorrect request body");
        }

        if (plantSetVolumeDTO.getPlantId() == null || plantSetVolumeDTO.getVolume() == null)
        {
            throw new IllegalArgumentException("Id or volume is missing");
        }

        if (plantSetVolumeDTO.getVolume() <= 0)
        {
            throw new IllegalArgumentException("Volume must be greater than 0");
        }

        if (plantsService.isNotOwner(plantSetVolumeDTO.getPlantId(), user.getUsername()))
        {
            throw new AccessDeniedException("You are not owner of this plant");
        }

        plantsService.setPlantVolume(plantSetVolumeDTO);
        return ResponseEntity.ok(Map.of("message", "Volume set successfully"));
    }

    @PutMapping("/api/v1/plant-set-name")
    public ResponseEntity<?> setPlantName(@RequestBody PlantSetNameDTO plantSetNameDTO, @AuthenticationPrincipal UserPrincipal user)
    {
        if (plantSetNameDTO == null)
        {
            throw new IllegalArgumentException("Incorrect request body");
        }

        if (plantSetNameDTO.getPlantId() == null || plantSetNameDTO.getName() == null || plantSetNameDTO.getName().isBlank())
        {
            throw new IllegalArgumentException("Id or name is missing");
        }

        if (plantCacheService.getPlantById(plantSetNameDTO.getPlantId()) == null)
        {
            throw new NoSuchElementException("No plant found with this id");
        }

        if (plantsService.isNotOwner(plantSetNameDTO.getPlantId(), user.getUsername()))
        {
            throw new AccessDeniedException("You are not owner of this plant");
        }

        plantsService.saveName(plantSetNameDTO.getPlantId(), plantSetNameDTO.getName());
        return ResponseEntity.ok(Map.of("message", "Successfully set plant name"));
    }

    @DeleteMapping("/api/v1/delete-plant/{plantId:\\d+}")
    public ResponseEntity<?> deletePlant(@PathVariable Integer plantId, @AuthenticationPrincipal UserPrincipal user)
    {
        if (plantId == null)
        {
            throw new IllegalArgumentException("Incorrect request body");
        }

        if (plantCacheService.getPlantById(plantId) == null)
        {
            throw new NoSuchElementException("No plant found with this id");
        }

        if (plantsService.isNotOwner(plantId, user.getUsername()))
        {
            throw new AccessDeniedException("You are not owner of this plant");
        }

        plantCacheService.deletePlant(plantId);
        return ResponseEntity.ok(Map.of("message", "Successfully deleted plant"));
    }

    @GetMapping("/api/v1/plants")
    public ResponseEntity<?> getOwnedPlants(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        String username = userPrincipal.getUsername();
        List<PlantResponseDTO> plants = plantsService.getPlantsByUsername(username);

        return ResponseEntity.ok(plants);
    }
}