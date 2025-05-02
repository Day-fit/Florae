package pl.Dayfit.Florae.DTOs;

import lombok.Data;
import pl.Dayfit.Florae.POJOs.Results;

import java.util.List;

@Data
public class PlantResponseDTO {
    private String bestMatch;
    private List<Results> results;
}
