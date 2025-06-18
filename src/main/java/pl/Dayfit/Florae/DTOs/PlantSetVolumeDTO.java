package pl.Dayfit.Florae.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PlantSetVolumeDTO {
    private Integer plantId;
    private Double volume;
}
