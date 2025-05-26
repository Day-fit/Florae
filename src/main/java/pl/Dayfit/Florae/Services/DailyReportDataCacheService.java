package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;
import pl.Dayfit.Florae.Repositories.JPA.DailyReportDataRepository;

import java.util.List;

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
