package pl.Dayfit.Florae.DTOs.Sensors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentSensorDataDTO {
    @NonNull
    private String type;
    @NonNull
    private Double value;
}
