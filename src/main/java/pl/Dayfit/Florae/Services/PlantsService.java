package pl.Dayfit.Florae.Services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.NoSuchElementException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.DTOs.PlantResponseDTO;
import pl.Dayfit.Florae.Repositories.PlantRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantsService {
    private final PlantRepository plantRepository;
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

    public String saveAndRecognise(ArrayList<MultipartFile> photos) throws NoSuchElementException, IOException {
        final MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

        photos.forEach(photo -> {
            multipartBodyBuilder.part("images", photo.getResource());
            multipartBodyBuilder.part("organs", "auto");
        });

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(multipartBodyBuilder.build(), headers);
        PlantResponseDTO response = restTemplate.postForObject("https://my-api.plantnet.org/v2/identify/all?include-related-images=true&no-reject=false&nb-results=1&lang=en&type=kt&api-key="+PLANT_NET_API_KEY, requestEntity, PlantResponseDTO.class);

        if (response != null)
        {
            String pid = response.getResults().getFirst().getSpecies().getScientificNameWithoutAuthor().toLowerCase();

            Plant plant = new Plant();
            plant.setSpeciesName(response.getBestMatch());
            plant.setPid(pid);
            plant.setPrimaryPhoto(Base64.getEncoder().encodeToString(photos.getFirst().getBytes()));

            plant.setRequirements(plantRequirementsService.getPlantRequirements(pid));

            plantRepository.save(plant);
            return plant.getSpeciesName();
        }

        log.debug("Florae cound not recognise any plant at given photos");
        throw new IllegalStateException("No matches found");
    }

    public Plant getPlantById(Integer id)
    {
        if (id==null) {
            return null;
        }

        return plantRepository.findById(id).orElse(null);
    }
}