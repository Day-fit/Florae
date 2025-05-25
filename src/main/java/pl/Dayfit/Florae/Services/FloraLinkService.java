package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorResponseDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorResponseDataDTO;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Entities.Sensors.CurrentReportData;
import pl.Dayfit.Florae.Entities.Sensors.CurrentSensorData;
import pl.Dayfit.Florae.Entities.Sensors.DailySensorData;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;
import pl.Dayfit.Florae.Repositories.JPA.FloraLinkRepository;
import pl.Dayfit.Florae.Repositories.JPA.DailyReportDataRepository;
import pl.Dayfit.Florae.Repositories.Redis.CurrentReportDataRepository;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FloraLinkService {
    private final DailyReportDataRepository dailyReportDataRepository;
    private final CurrentReportDataRepository currentReportDataRepository;
    private final FloraLinkRepository floraLinkRepository;
    private final FloraeUserCacheService floraeUserCacheService;

    public void handleReportUpload(List<DailySensorDataDTO> uploadedData, Authentication authentication)
    {
        FloraeUser floraeUser = floraeUserCacheService.getFloraeUser(((FloraeUser) authentication.getPrincipal()).getUsername());
        String apiKey = ((ApiKey) authentication.getCredentials()).getKeyValue();

        DailyReportData sensorReadings = new DailyReportData();
        sensorReadings.setFloraeUser(floraeUser);
        sensorReadings.setFloraLink(floraLinkRepository.findByApiKey(apiKey));

        uploadedData.forEach(data ->
        {
            DailySensorData sensorData = new DailySensorData(
                    null,
                    sensorReadings,
                    data.getType(),
                    data.getMinValue(),
                    data.getMinValueTimestamp(),
                    data.getMaxValue(),
                    data.getMaxValueTimestamp(),
                    data.getAverageValue()
            );

            sensorReadings.getSensorDataList().add(sensorData);
        });

        dailyReportDataRepository.save(sensorReadings);
    }

    public void handleCurrentDataUpload(List<CurrentSensorDataDTO> uploadedData, Authentication authentication)
    {
        String ownerUsername = ((FloraeUser) authentication.getPrincipal()).getUsername();
        String floraLinkId = ((ApiKey) authentication.getCredentials()).getLinkedFloraLink().getId().toString();
        CurrentReportData searchResult = currentReportDataRepository.findByFloraLinkId(floraLinkId);
        CurrentReportData currentReportData;

        if (searchResult == null)
        {
            String id = UUID.randomUUID().toString();
            currentReportData = new CurrentReportData(id, floraLinkId, ownerUsername, uploadedData.stream().map(data -> new CurrentSensorData(null, data.getType(), data.getValue())).toList());
        }

        else{
            currentReportDataRepository.delete(searchResult);
            currentReportData = new CurrentReportData(searchResult.getId(), floraLinkId, ownerUsername, uploadedData.stream().map(data -> new CurrentSensorData(null, data.getType(), data.getValue())).toList());
        }

        currentReportDataRepository.save(currentReportData);
    }

    public List<CurrentSensorResponseDataDTO> getAllCurrentData(String username)
    {
        return currentReportDataRepository.findAllByOwnerUsername(username).stream().map(data -> new CurrentSensorResponseDataDTO(
                Integer.valueOf(data.getFloraLinkId()),
                data.getCurrentSensorDataList().stream().map(
                        sensorData -> new CurrentSensorDataDTO(sensorData.getType(), sensorData.getValue())
                ).toList())).toList();
    }

    public List<DailySensorResponseDataDTO> getAllDayReportData(String username) {
        return dailyReportDataRepository.findAllSensorReadingsByOwnerUsername(username).stream()
                .map(data -> new DailySensorResponseDataDTO(data.getFloraLink().getId(), data.getSensorDataList().stream().map(sensorData -> {
                    DailySensorDataDTO dto = new DailySensorDataDTO();
                    dto.setType(sensorData.getType());
                    dto.setMaxValue(sensorData.getMaxValue());
                    dto.setMaxValueTimestamp(sensorData.getMaxValueTimestamp());
                    dto.setMinValue(sensorData.getMinValue());
                    dto.setMinValueTimestamp(sensorData.getMinValueTimestamp());
                    dto.setAverageValue(sensorData.getAverageValue());
                    return dto;
                }).toList())).toList();
    }

    public FloraLink registerFloraLink(FloraLink floraLink)
    {
        return floraLinkRepository.save(floraLink);
    }
}
