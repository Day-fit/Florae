package pl.Dayfit.Florae.Services;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Repositories.JPA.PlantRepository;

import java.util.List;

/**
 * <p>Service responsible for managing and caching the Plant entity in various ways</p>
 */

@Service
@AllArgsConstructor
public class PlantCacheService {
    private final PlantRepository plantRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "plants", key = "#id")
    public Plant getPlantById(int id)
    {
        return plantRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = "plants", key = "#plantId")
    public void deletePlant(Integer plantId) {
        plantRepository.deleteById(plantId);
    }

    @CachePut(value = "plants", key = "#plant.id")
    public Plant savePlant(Plant plant)
    {
        return plantRepository.save(plant);
    }

    @Cacheable(value = "user-plants", key = "#username")
    public List<Plant> getAllPlants(String username) {
        return plantRepository.getPlantsByUsername(username);
    }
}
