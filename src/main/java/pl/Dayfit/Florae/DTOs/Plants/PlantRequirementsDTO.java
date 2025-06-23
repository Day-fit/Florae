package pl.Dayfit.Florae.DTOs.Plants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PlantRequirementsDTO {
    private List<?> errors;

    private Double maxLightLux;
    private Double minLightLux;

    private Double maxTemp;
    private Double minTemp;

    private Double maxEnvHumid;
    private Double minEnvHumid;

    private Double maxSoilMoist;
    private Double minSoilMoist;
}