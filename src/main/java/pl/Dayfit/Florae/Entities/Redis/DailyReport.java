package pl.Dayfit.Florae.Entities.Redis;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import pl.Dayfit.Florae.Enums.SensorDataType;

import java.time.Instant;
import java.util.Map;

@RedisHash("DailyReport")
@Getter
@Setter
public class DailyReport {
    @Getter
    @Setter
    public static class DailyReadings
    {
        @NotNull
        private SensorDataType type;

        private Double maxValue;
        private Double minValue;
        private Double avgValue;

        private Instant minTimestamp;
        private Instant maxTimestamp;

        private Integer count;
    }

    @NotNull
    private String owner;
    @NotNull
    private Integer floraLinkId;
    @Id
    private String id;
    private Map<SensorDataType, DailyReadings> dailyReadings;
}
