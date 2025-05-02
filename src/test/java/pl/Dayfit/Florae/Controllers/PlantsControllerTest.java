package pl.Dayfit.Florae.Controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Services.PlantsService;

@ExtendWith(MockitoExtension.class)
class PlantsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlantsService plantsService;

    @InjectMocks
    private PlantsController plantsController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(plantsController)
                .build();
    }

    @Nested
    @DisplayName("uploadPhoto endpoint")
    class UploadPhoto {

        @Test
        @DisplayName("returns species name when photos are successfully processed")
        void returnsSpeciesNameWhenPhotosAreProcessed() throws Exception {
            MockMultipartFile photo = new MockMultipartFile("photos", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy".getBytes());
            when(plantsService.saveAndRecognise(any())).thenReturn("rose");

            mockMvc.perform(multipart("/api/v1/upload-photos").file(photo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.speciesName").value("rose"));
        }

        @Test
        @DisplayName("returns bad request when no photos are provided")
        void returnsBadRequestWhenNoPhotosProvided() throws Exception {
            mockMvc.perform(multipart("/api/v1/upload-photos"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns internal server error when IOException occurs")
        void returnsInternalServerErrorOnIOException() throws Exception {
            MockMultipartFile photo = new MockMultipartFile("photos", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy".getBytes());
            doThrow(IOException.class).when(plantsService).saveAndRecognise(any());

            mockMvc.perform(multipart("/api/v1/upload-photos").file(photo))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Failed at uploading photos"));
        }

        @Test
        @DisplayName("returns not found when no matches or requirements are found")
        void returnsNotFoundWhenNoMatchesOrRequirements() throws Exception {
            MockMultipartFile photo = new MockMultipartFile("photos", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy".getBytes());
            doThrow(NoSuchElementException.class).when(plantsService).saveAndRecognise(any());

            mockMvc.perform(multipart("/api/v1/upload-photos").file(photo))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("No matches or requirements found"));
        }
    }

    @Nested
    @DisplayName("getPlant endpoint")
    class GetPlant {

        @Test
        @DisplayName("returns plant details when plant is found")
        void returnsPlantDetailsWhenPlantIsFound() throws Exception {
            Plant plant = new Plant();
            plant.setId(1);
            plant.setSpeciesName("Rose");
            when(plantsService.getPlantById(1)).thenReturn(plant);

            mockMvc.perform(get("/api/v1/plant/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.speciesName").value("Rose"));
        }

        @Test
        @DisplayName("returns not found when plant does not exist")
        void returnsNotFoundWhenPlantDoesNotExist() throws Exception {
            when(plantsService.getPlantById(1)).thenReturn(null);

            mockMvc.perform(get("/api/v1/plant/1"))
                    .andExpect(status().isNotFound());
        }
    }
}