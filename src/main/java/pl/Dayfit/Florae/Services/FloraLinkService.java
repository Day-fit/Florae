package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraLinkReportDTO;
import pl.Dayfit.Florae.DTOs.SensorDataDTO;
import pl.Dayfit.Florae.Entities.Sensors.ReportSensorData;
import pl.Dayfit.Florae.Entities.Sensors.ReportData;
import pl.Dayfit.Florae.Repositories.JPA.SensorReadingsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FloraLinkService {
    private final SensorReadingsRepository sensorReadingsRepository;

    public void handleReportUpload(FloraLinkReportDTO uploadedData)
    {
        ReportData sensorReadings = new ReportData();

        for (SensorDataDTO data : uploadedData.getSensorDataList())
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

        sensorReadingsRepository.save(sensorReadings);
    }

    public List<ReportData> getAllData(String username) {
        return sensorReadingsRepository.findAllSensorReadingsByOwnerUsername(username);
    }
}
