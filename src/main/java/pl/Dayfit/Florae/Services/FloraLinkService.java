package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorResponseDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.DailySensorResponseDataDTO;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Entities.Sensors.CurrentReportData;
import pl.Dayfit.Florae.Entities.Sensors.CurrentSensorData;
import pl.Dayfit.Florae.Entities.Sensors.DailySensorData;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;
import pl.Dayfit.Florae.Repositories.Redis.CurrentReportDataRepository;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FloraLinkService {
    private final CurrentReportDataRepository currentReportDataRepository;
    private final FloraeUserCacheService floraeUserCacheService;
    private final FloraLinkCacheService cacheService;
    private final DailyReportDataCacheService dailyReportDataCacheService;

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

    public void handleCurrentDataUpload(List<CurrentSensorDataDTO> uploadedData, Authentication authentication)
    {
        String ownerUsername = ((FloraeUser) authentication
                .getPrincipal())
                .getUsername();

        String floraLinkId = ((ApiKey) authentication
                .getCredentials())
                .getLinkedFloraLink()
                .getId()
                .toString();

        CurrentReportData searchResult = currentReportDataRepository.findByFloraLinkId(floraLinkId);

        String sensorDataId = UUID.randomUUID().toString();

        if (searchResult == null)
        {
            String id = UUID.randomUUID().toString();
            searchResult = new CurrentReportData(id, floraLinkId, ownerUsername, uploadedData.stream()
                    .map(
                            data -> new CurrentSensorData(
                                    sensorDataId,
                                    data.getType(),
                                    data.getValue()
                            )
                    ).toList());
        }

        else{
            searchResult.getCurrentSensorDataList().clear();
            searchResult.getCurrentSensorDataList().addAll(uploadedData.stream().map(data -> new CurrentSensorData(sensorDataId, data.getType(), data.getValue())).toList());
        }

        currentReportDataRepository.save(searchResult);
    }

    public List<CurrentSensorResponseDataDTO> getCurrentDataReport(String username)
    {
        return currentReportDataRepository.findAllByOwnerUsername(username).stream().map(data -> new CurrentSensorResponseDataDTO(
                Integer.valueOf(data.getFloraLinkId()),
                data.getCurrentSensorDataList().stream().map(
                        sensorData -> new CurrentSensorDataDTO(sensorData.getType(), sensorData.getValue())
                ).toList())).toList();
    }

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
}
