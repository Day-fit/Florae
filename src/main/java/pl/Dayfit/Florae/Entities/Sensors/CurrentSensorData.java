package pl.Dayfit.Florae.Entities.Sensors;

import jakarta.persistence.GeneratedValue;
import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@RedisHash(value = "currentSensorData", timeToLive = 3600)
@Getter
@Setter
@AllArgsConstructor
public class CurrentSensorData {
    @Id
    @GeneratedValue
    private String id;
    private String type;
    private Double value;
}
