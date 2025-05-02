package pl.Dayfit.Florae.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PlantRequirementsResponseDTO {
    private List<?> errors;

    private Integer maxLightLux;
    private Integer minLightLux;

    private Integer maxTemp;
    private Integer minTemp;

    private Integer maxEnvHumid;
    private Integer minEnvHumid;

    private Integer maxSoilMoist;
    private Integer minSoilMoist;
}