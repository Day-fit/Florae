package pl.Dayfit.Florae.DTOs;

import lombok.Data;
import pl.Dayfit.Florae.Entities.FloraLink;

@Data
public class PlantResponseDTO
{
    private Integer id;
    private String owner;
    private String name;
    private String speciesName;
    private FloraLink linkedFloraLink;
    private String primaryPhoto;
    private PlantRequirementsDTO requirements;
}