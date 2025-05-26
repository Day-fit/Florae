package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;
import pl.Dayfit.Florae.Repositories.JPA.DailyReportDataRepository;

import java.util.List;

/**
 * Service responsible for caching and managing operations related to {@code DailyReportData}.
 * This service provides methods for retrieving, storing, and managing daily report data
 * while using caching mechanisms to improve performance.

 * Annotations:
 * - {@code @Service}: Indicates that this class is a Spring service component.
 * - {@code @RequiredArgsConstructor}: Generates a constructor with required fields, ensuring that
 *   dependencies are injected through constructor injection.

 * Methods:
 * - {@code getDailyReportData}: Fetches a {@code DailyReportData} entity by the identifier of the associated FloraLink.
 *   Caching is applied to store and retrieve data for later requests.
 * - {@code save}: Persists or updates a {@code DailyReportData} entity into the database and updates the cache.
 * - {@code findAllSensorReadingsByOwnerUsername}: Retrieves a list of {@code DailyReportData} entities
 *   associated with a specific user's username.
 */
@Service
@RequiredArgsConstructor
public class DailyReportDataCacheService {
    private final DailyReportDataRepository dailyReportDataRepository;

    @Cacheable(value = "flora-link-data", key = "#floraLinkId")
    public DailyReportData getDailyReportData(Integer floraLinkId)
    {
        return dailyReportDataRepository.findByFloraLink_Id(floraLinkId);
    }

    @CachePut(value = "flora-link-data", key = "#sensorReadings.floraLink.id")
    public DailyReportData save(DailyReportData sensorReadings) {
        return dailyReportDataRepository.save(sensorReadings);
    }

    public List<DailyReportData> findAllSensorReadingsByOwnerUsername(String username)
    {
        return dailyReportDataRepository.findAllSensorReadingsByOwnerUsername(username);
    }
}
