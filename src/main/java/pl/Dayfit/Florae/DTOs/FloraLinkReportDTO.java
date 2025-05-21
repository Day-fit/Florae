package pl.Dayfit.Florae.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class FloraLinkReportDTO {
    @Valid
    @Size(min = 1)
    private List<@Valid SensorDataDTO> SensorDataList;
}
