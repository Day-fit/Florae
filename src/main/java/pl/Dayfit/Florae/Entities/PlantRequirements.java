package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlantRequirements {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique=true, nullable=false)
    private String pid;

    private Integer maxTemperatureRequirements;
    private Integer minTemperatureRequirements;

    private Integer maxEnvHumid;
    private Integer minEnvHumid;

    private Integer maxSoilMoist;
    private Integer minSoilMoist;

    private Integer maxLightLux;
    private Integer minLightLux;
}
