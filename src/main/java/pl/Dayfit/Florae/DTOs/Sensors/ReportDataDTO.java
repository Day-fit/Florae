package pl.Dayfit.Florae.DTOs.Sensors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReportDataDTO {
    @Valid
    @Size(min = 1)
    private List<@Valid ReportSensorDataDTO> sensorDataList;
}
