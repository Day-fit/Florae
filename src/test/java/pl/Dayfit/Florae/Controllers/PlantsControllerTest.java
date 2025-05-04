package pl.Dayfit.Florae.Controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.Entities.FloraeUser;
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
                .addFilters(new SecurityContextPersistenceFilter())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private UserPrincipal principal() {
        FloraeUser u = new FloraeUser();
        u.setUsername("testUser");
        u.setPassword("pass");
        u.setRoles("ROLE_USER");
        return new UserPrincipal(u);
    }

    @Nested class UploadPhoto {
        @Test void shouldReturnSpecies() throws Exception {
            when(plantsService.saveAndRecognise(any(), eq("testUser"))).thenReturn("rose");

            MockMultipartFile photo = new MockMultipartFile(
                    "photos","photo.jpg",MediaType.IMAGE_JPEG_VALUE,"data".getBytes());

            mockMvc.perform(multipart("/api/v1/upload-photos")
                            .file(photo)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.speciesName").value("rose"));
        }
    }

    @Nested class GetPlant {
        @Test void shouldReturnPlant() throws Exception {
            Plant p = new Plant();
            p.setId(1);
            p.setSpeciesName("Rose");
            when(plantsService.getPlantById(1)).thenReturn(p);

            mockMvc.perform(get("/api/v1/plant/1")
                            .with(SecurityMockMvcRequestPostProcessors.user(principal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.speciesName").value("Rose"));
        }
    }
}
