package pl.Dayfit.Florae.Services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.Dayfit.Florae.DTOs.PlantRequirementsResponseDTO;
import pl.Dayfit.Florae.Entities.PlantRequirements;
import pl.Dayfit.Florae.Repositories.PlantRequirementsRepository;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantRequirementsService {
    private final PlantRequirementsRepository plantRequirementsRepository;
    private final RestTemplate restTemplate;

    @Value("${plant.book.api}")
    private String PLANT_BOOK_API;
    private final HttpHeaders headers = new HttpHeaders();

    @PostConstruct
    private void innit()
    {
        headers.add("Authorization", "Token " + PLANT_BOOK_API);
    }

    public PlantRequirements getPlantRequirements(String pid) throws NoSuchElementException
    {
        PlantRequirements plantRequirements = plantRequirementsRepository.getPlantRequirementsByPid(pid);

        if (plantRequirements != null)
        {
            return plantRequirements;
        }

        saveRequirements(pid);

        plantRequirements = plantRequirementsRepository.getPlantRequirementsByPid(pid);
        return plantRequirements;
    }

    private void saveRequirements(String pid) throws NoSuchElementException
    {
        ResponseEntity<PlantRequirementsResponseDTO> responseEntity;

        try {
            responseEntity = restTemplate.exchange("https://open.plantbook.io/api/v1/plant/detail/"+pid+"/", org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), PlantRequirementsResponseDTO.class);
        } catch (HttpClientErrorException exception) {
            log.debug("Plant requirement with pid {} not found", pid);
            throw new NoSuchElementException("Plant "+pid+" is not supported");
        }
        PlantRequirementsResponseDTO response = responseEntity.getBody();

        if (response == null)
        {
            log.debug("Plant requirement with pid {} not found", pid);
            throw new NoSuchElementException("Plant "+pid+" is not supported");
        }

        PlantRequirements plantRequirements = fetchPlantRequirements(pid, response);
        plantRequirementsRepository.save(plantRequirements);
    }

    private PlantRequirements fetchPlantRequirements(String pid, PlantRequirementsResponseDTO response) {
        PlantRequirements plantRequirements = new PlantRequirements();

        plantRequirements.setPid(pid);

        plantRequirements.setMaxEnvHumid(response.getMaxEnvHumid());
        plantRequirements.setMinEnvHumid(response.getMinEnvHumid());

        plantRequirements.setMaxSoilMoist(response.getMaxSoilMoist());
        plantRequirements.setMinSoilMoist(response.getMinSoilMoist());

        plantRequirements.setMinTemperatureRequirements(response.getMinTemp());
        plantRequirements.setMaxTemperatureRequirements(response.getMaxTemp());

        plantRequirements.setMaxLightLux(response.getMaxLightLux());
        plantRequirements.setMinLightLux(response.getMinLightLux());

        return plantRequirements;
    }
}