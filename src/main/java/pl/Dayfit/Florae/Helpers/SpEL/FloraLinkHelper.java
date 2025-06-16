package pl.Dayfit.Florae.Helpers.SpEL;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Services.FloraLinkCacheService;

@SuppressWarnings("unused")
@Component("floraLinkHelper")
@RequiredArgsConstructor
public class FloraLinkHelper {
    private final FloraLinkCacheService cacheService;

    public FloraeUser getOwner(FloraLink floraLink) {
        return cacheService.getOwner(floraLink);
    }
}
