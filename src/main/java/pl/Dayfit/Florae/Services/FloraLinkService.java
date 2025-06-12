package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.Dayfit.Florae.DTOs.FloraLinkSetNameDTO;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorResponseDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorResponseDataDTO;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Entities.Sensors.DailySensorData;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;
import pl.Dayfit.Florae.Events.CurrentDataUploadedEvent;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;

import java.util.ArrayList;
import java.util.List;

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
            report.setFloraeUser(floraeUserCacheService.getFloraeUser(((ApiKey) auth.getCredentials()).getLinkedFloraLink().getOwner().getUsername()));
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
        List<CurrentSensorDataDTO> uploadedData = event.data();

        String ownerUsername = ((FloraeUser) authentication
                .getPrincipal())
                .getUsername();

        String floraLinkId = ((ApiKey) authentication
                .getCredentials())
                .getLinkedFloraLink()
                .getId()
                .toString();

        CurrentSensorResponseDataDTO mappedDTO = new CurrentSensorResponseDataDTO(Integer.valueOf(floraLinkId), uploadedData);
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
    public void setName(FloraLinkSetNameDTO dto, String username) throws IllegalStateException{
        FloraeUser floraeUser = floraeUserCacheService.getFloraeUser(username);
        FloraLink floraLink = cacheService.getFloraLink(dto.getId());

        if (!floraLink.getOwner().equals(floraeUser))
        {
            throw new IllegalStateException("User is not the owner of this FloraLink! Cannot change name!");
        }

        floraLink.setName(dto.getName());

        cacheService.saveFloraLink(floraLink);
    }
}
