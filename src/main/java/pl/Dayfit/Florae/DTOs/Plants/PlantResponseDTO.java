package pl.Dayfit.Florae.DTOs.Plants;

import lombok.Data;
import pl.Dayfit.Florae.DTOs.FloraLinkResponseDTO;

@Data
public class PlantResponseDTO
{
    private Integer id;
    private String owner;
    private String name;
    private Double volume;
    private String speciesName;
    private FloraLinkResponseDTO linkedFloraLink;
    private String primaryPhoto;
    private PlantRequirementsDTO requirements;
}