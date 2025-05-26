package pl.Dayfit.Florae.Services;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Repositories.JPA.FloraLinkRepository;

/**
 * Service class for managing FloraLink entities with caching support.
 * This class handles operations related to fetching and saving FloraLink
 * entities while utilizing Spring's caching mechanisms for performance
 * optimization.
 * <p>
 * Responsibilities:
 * - Fetching a FloraLink entity by its ID, with results stored in a cache
 *   for subsequent retrievals.
 * - Saving or updating a FloraLink entity and updating the cache with the
 *   new or modified entity data.
 * <p>
 * Annotations:
 * - {@code @Service}: This class is a Spring Service component.
 * - {@code @AllArgsConstructor}: Automatically generates a constructor with
 *   all required arguments.
 * <p>
 * Caching:
 * - Utilizes Spring's {@code @Cacheable} and {@code @CachePut} annotations
 *   to manage caching for fetch and save operations on FloraLink entities.
 */
@Service
@AllArgsConstructor
public class FloraLinkCacheService {
    private final FloraLinkRepository floraLinkRepository;

    @Cacheable(value = "flora-link", key = "#floraLinkId")
    public FloraLink getFloraLink(Integer floraLinkId)
    {
        return floraLinkRepository.findById(floraLinkId).orElse(null);
    }

    @CachePut(value = "flora-link", key = "#floraLink.id")
    public FloraLink saveFloraLink(FloraLink floraLink)
    {
        return floraLinkRepository.save(floraLink);
    }
}
