package pl.Dayfit.Florae.POJOs;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class TemperatureRequirements
{
    private Double deg_f;
    private Double deg_c;
}