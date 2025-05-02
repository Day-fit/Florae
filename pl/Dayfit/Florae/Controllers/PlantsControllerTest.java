package pl.Dayfit.Florae.Controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.Dayfit.Florae.Services.PlantsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlantsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlantsService plantsService;
    
    @InjectMocks
    private PlantsController plantsController;
    
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(plantsController).build();
    }

    @Test
    void shouldReturnOkWhenValidPhotosAreUploaded() throws Exception {
        ArrayList<MockMultipartFile> mockFiles = new ArrayList<>();
        mockFiles.add(new MockMultipartFile("photos", "photo1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1".getBytes()));
        when(plantsService.saveAndRecognise(anyList())).thenReturn("speciesName");

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/upload-photos")
                        .file(mockFiles.get(0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speciesName").value("speciesName"));

        verify(plantsService, times(1)).saveAndRecognise(anyList());
    }

    @Test
    void shouldReturnBadRequestWhenPhotosAreMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/upload-photos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No photos provided"));

        verify(plantsService, times(0)).saveAndRecognise(anyList());
    }

    @Test
    void shouldReturnInternalServerErrorWhenIOExceptionOccurs() throws Exception {
        ArrayList<MockMultipartFile> mockFiles = new ArrayList<>();
        mockFiles.add(new MockMultipartFile("photos", "photo1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1".getBytes()));
        when(plantsService.saveAndRecognise(anyList())).thenThrow(new IOException());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/upload-photos")
                        .file(mockFiles.get(0)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed at uploading photos"));

        verify(plantsService, times(1)).saveAndRecognise(anyList());
    }

    @Test
    void shouldReturnNotFoundWhenNoMatchesAreFound() throws Exception {
        ArrayList<MockMultipartFile> mockFiles = new ArrayList<>();
        mockFiles.add(new MockMultipartFile("photos", "photo1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1".getBytes()));
        when(plantsService.saveAndRecognise(anyList())).thenThrow(new NoSuchElementException());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/upload-photos")
                        .file(mockFiles.get(0)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No matches or requirements found"));

        verify(plantsService, times(1)).saveAndRecognise(anyList());
    }

    @Test
    void shouldReturnBadRequestWhenIllegalArgumentExceptionOccurs() throws Exception {
        ArrayList<MockMultipartFile> mockFiles = new ArrayList<>();
        mockFiles.add(new MockMultipartFile("photos", "photo1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1".getBytes()));
        when(plantsService.saveAndRecognise(anyList())).thenThrow(new IllegalArgumentException());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/upload-photos")
                        .file(mockFiles.get(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid photo format"));

        verify(plantsService, times(1)).saveAndRecognise(anyList());
    }
}
