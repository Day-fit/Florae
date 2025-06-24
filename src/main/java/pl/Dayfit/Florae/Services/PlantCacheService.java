package pl.Dayfit.Florae.Services;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Events.ApiKeyRevokedEvent;
import pl.Dayfit.Florae.Repositories.JPA.ApiKeyRepository;
import pl.Dayfit.Florae.Repositories.JPA.PlantRepository;

import java.util.List;

/**
 * <p>Service responsible for managing and caching the Plant entity in various ways</p>
 */

@Service
@AllArgsConstructor
public class PlantCacheService {
    private final PlantRepository plantRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "plant", key = "#id")
    public Plant getPlantById(int id)
    {
        return plantRepository.findById(id).orElse(null);
    }

    @Transactional
    @CacheEvict(value = "plant", key = "#plantId")
    public void deletePlant(Integer plantId) {
        apiKeyRepository.deleteApiKeyByLinkedPlant_Id(plantId);
        plantRepository.deletePlantById(plantId); //I've tried many things to make this work with just `deleteById` but even when code runs fine locally, it fails at production. Feel free to change that
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
