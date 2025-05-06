package pl.Dayfit.Florae.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import pl.Dayfit.Florae.POJOs.Results;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantFetchDTO {
    private String bestMatch;
    private List<Results> results;
}
