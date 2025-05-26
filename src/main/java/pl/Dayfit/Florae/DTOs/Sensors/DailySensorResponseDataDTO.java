package pl.Dayfit.Florae.DTOs.Sensors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailySensorResponseDataDTO {
    private Integer sensorId;
    private List<DailySensorDataDTO> sensorData;
}
