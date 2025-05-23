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
import pl.Dayfit.Florae.DTOs.PlantRequirementsDTO;
import pl.Dayfit.Florae.Entities.PlantRequirements;
import pl.Dayfit.Florae.Repositories.JPA.PlantRequirementsRepository;

import java.util.NoSuchElementException;

/**
 * Service class responsible for managing plant requirements.
 * Provides functionality for retrieving and saving plant needs
 * such as light, temperature, humidity, and soil moisture levels.
 * Integrates with an external API for fetching detailed requirements
 * if data is not available in the local database.

 * Annotations:
 * - {@code @Service}: Declares this class as a Spring service component.
 * - {@code @Slf4j}: Enables logging capability within the class.
 * - {@code @RequiredArgsConstructor}: Generates a constructor for
 *   all final fields to support dependency injection.

 * Fields:
 * - plantRequirementsRepository: Repository interface for CRUD operations
 *   on PlantRequirements entities.
 * - restTemplate: Used for performing HTTP requests to external APIs.
 * - PLANT_BOOK_API: API token fetched from application configuration.
 * - headers: Pre-configured HTTP headers used for making API calls.

 * Methods:
 * - {@code innit}: Populates the {@code headers} object with necessary
 *   authorization credentials. Invoked post-construction of the service.
 * - {@code getPlantRequirements}: Fetches plant requirements by PID.
 *   If data is unavailable in the local database, the method retrieves
 *   it from an external API and stores it in the database.
 * - {@code saveRequirements}: Queries the external API for fetching plant
 *   requirements. Converts the response into a PlantRequirements entity
 *   and saves it in the database.
 * - {@code fetchPlantRequirements}: Maps the response from the external API
 *   to a PlantRequirements entity for further processing and storage.

 * Exceptions:
 * - {@code NoSuchElementException}: Thrown when the plant requirements
 *   for the specified PID cannot be found locally or in the external API.

 * Logs:
 * - Logs information about missing plant PIDs and debug-level messages during
 *   error scenarios in the external API calls.
 */
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
        ResponseEntity<PlantRequirementsDTO> responseEntity;

        try {
            responseEntity = restTemplate.exchange("https://open.plantbook.io/api/v1/plant/detail/"+pid+"/", org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), PlantRequirementsDTO.class);
        } catch (HttpClientErrorException exception) {
            log.debug("Plant requirement with pid {} not found", pid);
            throw new NoSuchElementException("Plant "+pid+" is not supported");
        }
        PlantRequirementsDTO response = responseEntity.getBody();

        if (response == null)
        {
            log.debug("Plant requirement with pid {} not found", pid);
            throw new NoSuchElementException("Plant "+pid+" is not supported");
        }

        PlantRequirements plantRequirements = fetchPlantRequirements(pid, response);
        plantRequirementsRepository.save(plantRequirements);
    }

    private PlantRequirements fetchPlantRequirements(String pid, PlantRequirementsDTO response) {
        PlantRequirements plantRequirements = new PlantRequirements();

        plantRequirements.setPid(pid);

        plantRequirements.setMaxEnvHumid(response.getMaxEnvHumid());
        plantRequirements.setMinEnvHumid(response.getMinEnvHumid());

        plantRequirements.setMaxSoilMoist(response.getMaxSoilMoist());
        plantRequirements.setMinSoilMoist(response.getMinSoilMoist());

        plantRequirements.setMinTemp(response.getMinTemp());
        plantRequirements.setMaxTemp(response.getMaxTemp());

        plantRequirements.setMaxLightLux(response.getMaxLightLux());
        plantRequirements.setMinLightLux(response.getMinLightLux());

        return plantRequirements;
    }
}