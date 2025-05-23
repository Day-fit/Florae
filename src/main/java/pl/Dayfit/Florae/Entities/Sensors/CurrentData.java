package pl.Dayfit.Florae.Entities.FloraLink;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RedisHash("current_data")
@Getter
@Setter
@AllArgsConstructor
public class CurrentData {
    @Id
    private String id;
    private List<CurrentSensorData> currentSensorDataList = new ArrayList<>();
}
