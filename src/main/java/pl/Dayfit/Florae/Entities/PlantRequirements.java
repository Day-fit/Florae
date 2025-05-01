package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.Dayfit.Florae.Enums.AtmosphericHumidity;
import pl.Dayfit.Florae.Enums.SoilHumidity;
import pl.Dayfit.Florae.POJOs.TemperatureRequirements;

@Entity
@Getter
@Setter
public class PlantRequirements {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique=true, nullable=false)
    private String slug;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "deg_f", column = @Column(name = "min_deg_f")),
            @AttributeOverride(name = "deg_c", column = @Column(name = "min_deg_c")),
    })
    private TemperatureRequirements minTemperatureRequirements;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "deg_f", column = @Column(name = "max_deg_f")),
            @AttributeOverride(name = "deg_c", column = @Column(name = "max_deg_c")),
    })
    private TemperatureRequirements maxTemperatureRequirements;

    private Double phMaximum;
    private Double phMinimum;

    @Enumerated(EnumType.STRING)
    private AtmosphericHumidity recommendedHumidity;
    @Enumerated(EnumType.STRING)
    private SoilHumidity recommendedSoilState;
}
