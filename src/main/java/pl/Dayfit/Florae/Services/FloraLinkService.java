package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraLinkReportDTO;
import pl.Dayfit.Florae.DTOs.SensorDataDTO;
import pl.Dayfit.Florae.Entities.SensorData;
import pl.Dayfit.Florae.Entities.SensorReadings;
import pl.Dayfit.Florae.Repositories.SensorReadingsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FloraLinkService {
    private final SensorReadingsRepository sensorReadingsRepository;

    public void handleReportUpload(FloraLinkReportDTO uploadedData)
    {
        SensorReadings sensorReadings = new SensorReadings();

        for (SensorDataDTO data : uploadedData.getSensorDataList())
        {
            SensorData sensorData = new SensorData(
                    null,
                    sensorReadings,
                    data.getType(),
                    data.getMinValue(),
                    data.getMinValueTimestamp(),
                    data.getMaxValue(),
                    data.getMaxValueTimestamp(),
                    data.getAverageValue(),
                    data.getAverageValueTimestamp()
            );

            sensorReadings.getSensorDataList().add(sensorData);
        }

        sensorReadingsRepository.save(sensorReadings);
    }

    public List<SensorReadings> getAllData(String username) {
        return sensorReadingsRepository.findAllSensorReadingsByOwnerUsername(username);
    }
}
