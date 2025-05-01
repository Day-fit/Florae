package pl.Dayfit.Florae.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import pl.Dayfit.Florae.POJOs.PlantGrowth;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantRequirementsResponseDTO {
    private PlantData data;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlantData
    {
        private PlantMainSpecies main_species;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PlantMainSpecies
        {
            private PlantGrowth growth;
        }
    }
}