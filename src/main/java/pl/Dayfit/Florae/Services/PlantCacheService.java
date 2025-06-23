package pl.Dayfit.Florae.Services;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Events.ApiKeyRevokedEvent;
import pl.Dayfit.Florae.Repositories.JPA.PlantRepository;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>Service responsible for managing and caching the Plant entity in various ways</p>
 */

@Service
@AllArgsConstructor
public class PlantCacheService {
    private final PlantRepository plantRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "plant", key = "#id")
    public Plant getPlantById(int id)
    {
        return plantRepository.findById(id).orElse(null);
    }

    @Transactional
    @CacheEvict(value = "plant", key = "#plantId")
    public void deletePlant(Integer plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new NoSuchElementException("Plant not found"));

        FloraeUser user = plant.getLinkedUser();
        if (user != null) {
            user.getLinkedPlants().remove(plant);
        }

        plantRepository.delete(plant);
    }

    @CachePut(value = "plant", key = "#plant.id")
    public Plant savePlant(Plant plant)
    {
        return plantRepository.save(plant);
    }

    @Cacheable(value = "plants", key = "#username")
    public List<Plant> getAllPlants(String username) {
        return plantRepository.getPlantsByUsername(username);
    }

    @SuppressWarnings("unused")
    @TransactionalEventListener
    @CacheEvict(value = "plant", key = "#event.apiKey().getLinkedPlant().getId()")
    public void handleApiKeyRevocation(ApiKeyRevokedEvent event) {
    }
}
