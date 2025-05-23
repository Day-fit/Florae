package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.Sensors.ReportDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.ReportSensorDataDTO;
import pl.Dayfit.Florae.Entities.Sensors.ReportSensorData;
import pl.Dayfit.Florae.Entities.Sensors.ReportData;
import pl.Dayfit.Florae.Repositories.JPA.ReportDataRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FloraLinkService {
    private final ReportDataRepository reportDataRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void handleReportUpload(ReportDataDTO uploadedData)
    {
        ReportData sensorReadings = new ReportData();

        for (ReportSensorDataDTO data : uploadedData.getSensorDataList())
        {
            ReportSensorData sensorData = new ReportSensorData(
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
        }

        reportDataRepository.save(sensorReadings);
    }

    public void handleCurrentDataUpload(ReportDataDTO uploadedData)
    {
        ReportData sensorReadings = new ReportData();
    }

    public List<ReportData> getAllReportData(String username) {
        return reportDataRepository.findAllSensorReadingsByOwnerUsername(username);
    }
}
