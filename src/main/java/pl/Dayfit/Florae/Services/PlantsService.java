package pl.Dayfit.Florae.Services;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.Dayfit.Florae.DTOs.PlantRequirementsDTO;
import pl.Dayfit.Florae.DTOs.PlantResponseDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.DTOs.PlantFetchDTO;
import pl.Dayfit.Florae.Entities.PlantRequirements;
import pl.Dayfit.Florae.Repositories.FloraeUserRepository;
import pl.Dayfit.Florae.Repositories.PlantRepository;
import pl.Dayfit.Florae.Utils.ImageOptimizer;


/**
 * Service class responsible for handling operations related to plants.
 * This class includes features for recognizing plants, saving plant data,
 * and fetching plants associated with users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlantsService {
    private final PlantRepository plantRepository;
    private final FloraeUserRepository floraeUserRepository;
    private final PlantRequirementsService plantRequirementsService;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers = new HttpHeaders();

    @PostConstruct
    private void init()
    {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    }

    @Value("${plant.net.api}")
    private String PLANT_NET_API_KEY;

    @Transactional
    public String saveAndRecognise(List<MultipartFile> photos, String username) throws NoSuchElementException, IOException {
        final MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        final FloraeUser floraeUser = floraeUserRepository.findByUsername(username);

        for (MultipartFile photo : photos)
        {
            multipartBodyBuilder.part("images", new ByteArrayResource(ImageOptimizer.optimizeImage(photo, .7f)) {
                @Override
                public String getFilename() {
                    return "image." + photo.getContentType();
                }
            });
            multipartBodyBuilder.part("organs", "auto");
        }

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(multipartBodyBuilder.build(), headers);

        PlantFetchDTO response;

        try{
            response = restTemplate.postForObject("https://my-api.plantnet.org/v2/identify/all?include-related-images=true&no-reject=false&nb-results=1&lang=en&type=kt&api-key="+PLANT_NET_API_KEY, requestEntity, PlantFetchDTO.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("PlantNet API error: Status Code: {}, Message: {}", ex.getStatusCode(), ex.getMessage());
            throw new IllegalStateException("External API call failed");
        }

        if (response != null)
        {
            String pid = response.getResults().getFirst().getSpecies().getScientificNameWithoutAuthor().toLowerCase();

            Plant plant = new Plant();
            plant.setSpeciesName(response.getBestMatch());
            plant.setPid(pid);
            plant.setPrimaryPhoto(Base64.getEncoder().encodeToString(ImageOptimizer.optimizeImage(photos.getFirst(), .7f)));
            plant.setLinkedUser(floraeUser);
            plant.setRequirements(plantRequirementsService.getPlantRequirements(pid));

            plantRepository.save(plant);
            return plant.getSpeciesName();
        }

        log.debug("Florae could not recognise any plant at given photos");
        throw new IllegalStateException("No matches found");
    }

    public PlantResponseDTO getPlantById(Integer id)
    {
        Plant plant = plantRepository.findById(id).orElse(null);
        return mapPlantDTO(plant);
    }

    @Transactional(readOnly = true)
    public List<PlantResponseDTO> getPlantsByUsername(String username)
    {
        return plantRepository.getPlantsByUsername(username).stream().map(this::mapPlantDTO).toList();
    }

    private PlantResponseDTO mapPlantDTO(Plant plant)
    {
        if(plant == null){
            return null;
        }

        PlantRequirements plantRequirements = plant.getRequirements();
        PlantResponseDTO mappedElement = new PlantResponseDTO();
        mappedElement.setOwner(plant.getLinkedUser().getUsername());
        mappedElement.setSpeciesName(plant.getSpeciesName());
        mappedElement.setPrimaryPhoto(plant.getPrimaryPhoto());
        mappedElement.setLinkedEsp(plant.getLinkedEsp());
        mappedElement.setRequirements(
                new PlantRequirementsDTO(
                        null,
                        plantRequirements.getMaxLightLux(),
                        plantRequirements.getMinLightLux(),
                        plantRequirements.getMaxTemp(),
                        plantRequirements.getMinTemp(),
                        plantRequirements.getMaxEnvHumid(),
                        plantRequirements.getMinEnvHumid(),
                        plantRequirements.getMaxSoilMoist(),
                        plantRequirements.getMinSoilMoist()
                )
        );

        return mappedElement;
    }
}