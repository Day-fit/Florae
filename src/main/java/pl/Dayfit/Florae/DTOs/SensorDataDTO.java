package pl.Dayfit.Florae.DTOs;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SensorDataDTO {
    private @NonNull String type;

    private @NonNull Double minValue;
    private @NonNull Instant minValueTimestamp;

    private @NonNull Double maxValue;
    private @NonNull Instant maxValueTimestamp;

    private @NonNull Double averageValue;
    private @NonNull Instant averageValueTimestamp;
}
