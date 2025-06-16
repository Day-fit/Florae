package pl.Dayfit.Florae.Services;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.Dayfit.Florae.DTOs.FloraLinkResponseDTO;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Events.ApiKeyRevokedEvent;
import pl.Dayfit.Florae.Repositories.JPA.FloraLinkRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing FloraLink entities with caching support.
 * This class handles operations related to fetching and saving FloraLink
 * entities while using Spring's caching mechanisms for performance
 * optimization.
 * <p>
 * Responsibilities:
 * - Fetching a FloraLink entity by its ID, with results stored in a cache
 *   for later retrievals.
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

    @Transactional(readOnly = true)
    @Cacheable(value = "flora-links", key = "#ownerId")
    public List<FloraLinkResponseDTO> getOwnedFloraLinks(Integer ownerId)
    {
        return floraLinkRepository.findByOwnerId(ownerId).orElse(new ArrayList<>())
                .stream()
                .map(elem -> new FloraLinkResponseDTO(elem.getId(), elem.getName()))
                .toList();
    }

    @Transactional
    @CacheEvict(value = "flora-links", key = "@floraLinkHelper.getOwner(#floraLink)")
    @CachePut(value = "flora-link", key = "#floraLink.id")
    public FloraLink saveFloraLink(FloraLink floraLink)
    {
        return floraLinkRepository.save(floraLink);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "owner", key = "#floraLink.id")
    public FloraeUser getOwner(FloraLink floraLink) {
        return floraLinkRepository.findOwnerByFloraLink(floraLink).orElse(null);
    }

    @TransactionalEventListener
    @Caching(evict = {
            @CacheEvict(value = "flora-links", key = "@floraLinkHelper.getOwner(#event.apiKey().linkedFloraLink)"),
            @CacheEvict(value = "flora-link", key = "#event.apiKey().linkedFloraLink.id")
    })
    public void handleApiKeyRevocation(ApiKeyRevokedEvent event) {
        floraLinkRepository.delete(event.apiKey().getLinkedFloraLink());
    }
}
