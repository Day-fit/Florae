package pl.Dayfit.Florae.DTOs;

import lombok.Data;
import pl.Dayfit.Florae.Entities.Esp;

@Data
public class PlantResponseDTO
{
    private String owner;
    private String speciesName;
    private Esp linkedEsp;
    private String primaryPhoto;
    private PlantRequirementsDTO requirements;
}