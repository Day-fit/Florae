package pl.Dayfit.Florae.Controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MvcResult;
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

    @Test void shouldReturnSpecies() throws Exception {
        Plant plant = new Plant();
        plant.setId(1);
        plant.setSpeciesName("rose");

        when(plantsService.saveAndRecognise(any(), eq("testUser"))).thenReturn(plant);

        MockMultipartFile photo = new MockMultipartFile(
                "photos","photo.jpg",MediaType.IMAGE_JPEG_VALUE,"data".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/v1/add-plant")
                        .file(photo)
                        .with(SecurityMockMvcRequestPostProcessors.user(principal())))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speciesName").value("rose"));
    }
}
