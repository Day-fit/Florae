package pl.Dayfit.Florae.Entities.Sensors;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@RedisHash(value = "current_data", timeToLive = 3600)
@Getter
@Setter
@AllArgsConstructor
public class CurrentReportData {
    @Id
    private String id;

    @Indexed
    private String floraLinkId;

    @Indexed
    private String ownerUsername;
    private List<CurrentSensorData> currentSensorDataList;
}
