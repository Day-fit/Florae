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
import pl.Dayfit.Florae.Entities.*;
import pl.Dayfit.Florae.Entities.Redis.DailyReport;
import pl.Dayfit.Florae.Enums.CommandType;
import pl.Dayfit.Florae.Enums.SensorDataType;
import pl.Dayfit.Florae.Events.CurrentDataUploadedEvent;
import pl.Dayfit.Florae.Exceptions.DeviceOfflineException;
import pl.Dayfit.Florae.Repositories.Redis.DailyReportRepository;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;
import pl.Dayfit.Florae.Services.WebSockets.SessionService;

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
    private final RedisTemplate<String, Object> redisTemplate;
    private final DailyReportRepository dailyReportRepository;
    private final SessionService sessionService;

    @Transactional
    @EventListener
    public void handleCurrentDataUpload(CurrentDataUploadedEvent event)
    {
        Authentication authentication = event.authentication();
        Plant linkedPlant = event.plant();
        List<CurrentSensorDataDTO> uploadedData = event.data();
        CurrentSensorDataDTO soilMoistureData = uploadedData.stream().filter(data -> data.getType().equals(SensorDataType.SOIL_MOISTURE.toString())).findFirst().orElse(null);
        Double minimalSoilMoisture = linkedPlant.getPotVolume();

        String ownerUsername = ((FloraeUser) authentication
                .getPrincipal())
                .getUsername();

        Integer floraLinkId = ((ApiKey) authentication
                .getCredentials())
                .getLinkedFloraLink()
                .getId();

        if (soilMoistureData != null && minimalSoilMoisture != null && linkedPlant.getRequirements().getMinSoilMoist() > soilMoistureData.getValue())
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
        List<Plant> ownedPlants = floraeUserCacheService.getFloraeUser(username).getLinkedPlants();

        return ownedPlants.stream().map(plant ->
        {
            DailyReport report = dailyReportRepository.findDailyReportById(plant.getId().toString());

            if (report == null)
            {
                return null;
            }

            return new DailySensorResponseDataDTO(report.getFloraLinkId(), report.getDailyReadings().values().stream().map(dailyReading ->
                    new DailySensorDataDTO(
                            dailyReading.getType().toString(),
                            dailyReading.getMinValue(),
                            dailyReading.getMinTimestamp(),
                            dailyReading.getMaxValue(),
                            dailyReading.getMaxTimestamp(),
                            dailyReading.getAvgValue()
                    )).toList());
        }).toList();
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

        if (sessionService.getFloralinkSessionById(floralinkId.toString()) == null)
        {
            throw new DeviceOfflineException("FloraLink with ID " + floralinkId + " is not connected to WebSocket channel");
        }

        redisTemplate.convertAndSend("floralink." + floralinkId, new Command(CommandType.ENABLE_BLE));
    }
}
