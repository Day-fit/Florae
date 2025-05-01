package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.Dayfit.Florae.DTOs.PlantRequirementsResponseDTO;
import pl.Dayfit.Florae.Entities.PlantRequirements;
import pl.Dayfit.Florae.Enums.AtmosphericHumidity;
import pl.Dayfit.Florae.Enums.SoilHumidity;
import pl.Dayfit.Florae.POJOs.PlantGrowth;
import pl.Dayfit.Florae.Repositories.PlantRequirementsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantRequirementsService {
    private final PlantRequirementsRepository plantRequirementsRepository;
    private final RestTemplate restTemplate;

    @Value("${trefle.api.key}")
    private String TREFLE_API_KEY;

    public PlantRequirements getPlantRequirements(String slug) throws IllegalStateException
    {
        PlantRequirements plantRequirements = plantRequirementsRepository.getPlantRequirementsBySlug(slug);

        if (plantRequirementsRepository.getPlantRequirementsBySlug(slug) != null)
        {
            return plantRequirements;
        }

        saveRequirements(slug);

        plantRequirements = plantRequirementsRepository.getPlantRequirementsBySlug(slug);
        return plantRequirements;
    }

    private void saveRequirements(String slug) throws IllegalStateException
    {
        PlantRequirementsResponseDTO response = restTemplate.getForObject("https://trefle.io/api/v1/plants/"+slug+"?token="+ TREFLE_API_KEY, PlantRequirementsResponseDTO.class);

        if (response == null || response.getData() == null || response.getData().getMain_species().getGrowth() == null)
        {
            log.debug("Plant requirement slug {} not found", slug);
            throw new IllegalStateException("Plant "+slug+" is not supported");
        }

        PlantRequirements plantRequirements = fetchPlantRequirements(slug, response);

        plantRequirementsRepository.save(plantRequirements);
    }

    private PlantRequirements fetchPlantRequirements(String slug, PlantRequirementsResponseDTO response) {
        PlantGrowth responseGrowth = response.getData().getMain_species().getGrowth();
        PlantRequirements plantRequirements = new PlantRequirements();
        plantRequirements.setSlug(slug);

        plantRequirements.setRecommendedHumidity(getRecommendedHumidityEnum(responseGrowth.getAtmospheric_humidity()));

        plantRequirements.setMinTemperatureRequirements(responseGrowth.getMinimum_temperature());
        plantRequirements.setMaxTemperatureRequirements(responseGrowth.getMaximum_temperature());

        plantRequirements.setRecommendedSoilState(getRecommendedSoilHumidityEnum(responseGrowth.getSoil_humidity()));

        plantRequirements.setPhMinimum(responseGrowth.getPh_minimum());
        plantRequirements.setPhMaximum(responseGrowth.getPh_maximum());

        return plantRequirements;
    }

    private SoilHumidity getRecommendedSoilHumidityEnum(Integer rawData)
    {
        if (rawData == null)
        {
            return SoilHumidity.UNKNOWN;
        }

        int humidity = Math.round((float) rawData / 2);

        return SoilHumidity.values()[humidity];
    }

    private AtmosphericHumidity getRecommendedHumidityEnum(Integer rawData)
    {
        if (rawData == null)
        {
            return AtmosphericHumidity.UNKNOWN;
        }

        int humidity = Math.round((float) rawData / 2);

        return AtmosphericHumidity.values()[humidity];
    }
}