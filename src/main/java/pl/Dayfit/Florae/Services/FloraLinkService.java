package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.Dayfit.Florae.DTOs.FloraLinkSetNameDTO;
import pl.Dayfit.Florae.DTOs.Sensors.*;
import pl.Dayfit.Florae.DTOs.Sensors.Commands.Command;
import pl.Dayfit.Florae.DTOs.Sensors.Commands.CommandMessage;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Entities.Sensors.DailySensorData;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;
import pl.Dayfit.Florae.Enums.CommandType;
import pl.Dayfit.Florae.Events.CurrentDataUploadedEvent;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service class responsible for handling operations related to FloraLink data,
 * including current sensor data and daily sensor data. This class performs data
 * processing, transformation, and persistence tasks for uploaded data and retrieval
 * of reports.
 */
@Service
@RequiredArgsConstructor
public class FloraLinkService {
    private final FloraeUserCacheService floraeUserCacheService;
    private final FloraLinkCacheService cacheService;
    private final DailyReportDataCacheService dailyReportDataCacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SOIL_MOISTURE_TYPE_NAME = "soil_moisture";

    @Transactional
    public void handleReportUpload(List<DailySensorDataDTO> uploadedData, Authentication auth) {
        Integer floraLinkId = ((ApiKey) auth.getCredentials())
                .getLinkedFloraLink()
                .getId();

        DailyReportData report = dailyReportDataCacheService.getDailyReportData(floraLinkId);

        if (report == null) {
            report = new DailyReportData();
            report.setSensorDataList(new ArrayList<>());
            report.setFloraLink(cacheService.getFloraLink(floraLinkId));
            report.setFloraeUser(floraeUserCacheService.getFloraeUser(((FloraeUser) auth.getPrincipal()).getUsername()));
        }

        report.getSensorDataList().clear();

        DailyReportData finalReport = report;
        report.getSensorDataList().addAll(uploadedData.stream().map(data ->
            new DailySensorData(
                    null,
                    finalReport,
                    data.getType(),
                    data.getMinValue(),
                    data.getMinValueTimestamp(),
                    data.getMaxValue(),
                    data.getMaxValueTimestamp(),
                    data.getAverageValue()
            )).toList());

        dailyReportDataCacheService.save(report);
    }

    @Transactional
    @EventListener
    public void handleCurrentDataUpload(CurrentDataUploadedEvent event)
    {
        Authentication authentication = event.authentication();
        Plant linkedPlant = event.plant();
        List<CurrentSensorDataDTO> uploadedData = event.data();
        CurrentSensorDataDTO soilMoistureData = uploadedData.stream().filter(data -> data.getType().equals(SOIL_MOISTURE_TYPE_NAME)).findFirst().orElse(null);

        String ownerUsername = ((FloraeUser) authentication
                .getPrincipal())
                .getUsername();

        Integer floraLinkId = ((ApiKey) authentication
                .getCredentials())
                .getLinkedFloraLink()
                .getId();

        if (soilMoistureData != null && linkedPlant.getRequirements().getMinSoilMoist() > soilMoistureData.getValue())
        {
            redisTemplate.convertAndSend("floralink." + floraLinkId, new CommandMessage(
                    CommandType.WATERING,
                    calculateWaterToAdd(
                            linkedPlant.getPotVolume(),
                            ((double) linkedPlant.getRequirements().getMinSoilMoist() + linkedPlant.getRequirements().getMaxSoilMoist()) / 2,
                            soilMoistureData.getValue()
                    )
                )
            );
        }

        CurrentSensorResponseDataDTO mappedDTO = new CurrentSensorResponseDataDTO(floraLinkId, uploadedData);
        redisTemplate.convertAndSend("user." + ownerUsername, mappedDTO);
    }

    @Transactional(readOnly = true)
    public List<DailySensorResponseDataDTO> getDailyDataReport(String username) {
        return dailyReportDataCacheService.findAllSensorReadingsByOwnerUsername(username).stream()
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

    @Transactional
    public void setName(FloraLinkSetNameDTO dto, String username) throws AccessDeniedException {
        FloraeUser floraeUser = floraeUserCacheService.getFloraeUser(username);
        FloraLink floraLink = cacheService.getFloraLink(dto.getId());

        if (!cacheService.getOwner(floraLink).equals(floraeUser))
        {
            throw new AccessDeniedException("User is not the owner of this FloraLink! Cannot change name!");
        }

        floraLink.setName(dto.getName());

        cacheService.saveFloraLink(floraLink);
    }

    /**
     * Calculates how much water needs to be added to achieve the recommended moisture value
     * @param capacityLiters the pot capacity in liters
     * @param recommendedMoisture the recommended soil moisture value in percents
     * @param currentMoisture the current soil moisture value in percents
     * @return the water volume that needs to be added (in milliliters)
     */
    private double calculateWaterToAdd(double capacityLiters, double recommendedMoisture, double currentMoisture) {
        double neededHumidity = recommendedMoisture - currentMoisture;
        return capacityLiters * neededHumidity * 10; // (neededHumidity / 100.0) * 1000.0 = 10 * neededHumidity
    }

    public void handleEnablingBle(Integer floralinkId, String owner) {
        FloraLink floraLink = cacheService.getFloraLink(floralinkId);

        if(floraLink == null)
        {
            throw new NoSuchElementException("FloraLink with ID " + floralinkId + " not found ");
        }

        if(!cacheService.getOwner(floraLink).getUsername().equals(owner))
        {
            throw new AccessDeniedException("User is not the owner of this device! Cannot enable BLE!");
        }

        redisTemplate.convertAndSend("floralink." + floralinkId, new Command(CommandType.ENABLE_BLE));
    }
}
