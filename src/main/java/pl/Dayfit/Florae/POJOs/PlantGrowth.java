package pl.Dayfit.Florae.POJOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantGrowth {
    private Integer atmospheric_humidity;

    private TemperatureRequirements minimum_temperature;
    private TemperatureRequirements maximum_temperature;

    private Double ph_maximum;
    private Double ph_minimum;

    private Integer soil_humidity;
}