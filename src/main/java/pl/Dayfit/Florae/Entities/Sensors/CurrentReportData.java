package pl.Dayfit.Florae.Entities.Sensors;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@RedisHash(value = "current_data", timeToLive = 3600)
@Getter
@Setter
@AllArgsConstructor
public class CurrentReportData {
    @Id
    private String id;
    private List<CurrentSensorData> currentSensorDataList;
}
