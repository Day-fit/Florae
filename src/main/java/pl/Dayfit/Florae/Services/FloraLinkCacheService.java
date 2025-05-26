package pl.Dayfit.Florae.Services;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Repositories.JPA.FloraLinkRepository;

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
