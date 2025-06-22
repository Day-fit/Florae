package pl.Dayfit.Florae.DTOs.Sensors;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailySensorDataDTO {
    private @NonNull String type;

    private @NonNull Double minValue;
    private @NonNull Instant minValueTimestamp;

    private @NonNull Double maxValue;
    private @NonNull Instant maxValueTimestamp;

    private @NonNull Double averageValue;
}
