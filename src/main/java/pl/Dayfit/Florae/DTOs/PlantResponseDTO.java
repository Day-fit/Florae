package pl.Dayfit.Florae.DTOs;

import lombok.Data;

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