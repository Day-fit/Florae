package pl.Dayfit.Florae.Controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.setup.mockito.MockMvcBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.Dayfit.Florae.Entities.Esp;
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Entities.PlantRequirements;
import pl.Dayfit.Florae.Services.PlantsService;
