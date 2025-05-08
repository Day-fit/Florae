package pl.Dayfit.Florae.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.Dayfit.Florae.DTOs.FloraeUserRequestDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Services.FloraeUserService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FloraeUserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private FloraeUserService userService;

    @InjectMocks
    private FloraeUserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    private FloraeUserRequestDTO buildDto(String u, String e, String p) {
        FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
        dto.setUsername(u);
        dto.setEmail(e);
        dto.setPassword(p);
        return dto;
    }

    @Test
    void registerUser_validData_returnsOk() throws Exception {
        FloraeUserRequestDTO dto = buildDto("user1", "a@b.com", "pass123");
        doNothing().when(userService).registerUser(any());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully. Please check your email for verification."));

        verify(userService).registerUser(any());
    }

    @Test
    void registerUser_duplicateKey_returnsConflict() throws Exception {
        FloraeUserRequestDTO dto = buildDto("user1", "a@b.com", "pass123");
        doThrow(new DuplicateKeyException("OK"))
                .when(userService).registerUser(any());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("OK"));

        verify(userService).registerUser(any());
    }

    @Test
    void registerUser_invalidUsername_returnsBadRequest() throws Exception {
        FloraeUserRequestDTO dto1 = buildDto(null, "a@b.com", "pass");
        FloraeUserRequestDTO dto2 = buildDto("   ", "a@b.com", "pass");
        FloraeUserRequestDTO dto3 = buildDto("bad name!", "a@b.com", "pass");
        String tooLong = "a".repeat(FloraeUser.MAX_USERNAME_LENGTH + 1);
        FloraeUserRequestDTO dto4 = buildDto(tooLong, "a@b.com", "pass");

        for (FloraeUserRequestDTO dto : new FloraeUserRequestDTO[]{dto1, dto2, dto3, dto4}) {
            mockMvc.perform(post("/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username is not valid"));
        }
        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_invalidEmail_returnsBadRequest() throws Exception {
        FloraeUserRequestDTO dto1 = buildDto("user", null, "pass");
        FloraeUserRequestDTO dto2 = buildDto("user", "   ", "pass");
        FloraeUserRequestDTO dto3 = buildDto("user", "no-at-char", "pass");
        String tooLongEmail = "a".repeat(FloraeUser.MAX_EMAIL_LENGTH + 1) + "@x.com";
        FloraeUserRequestDTO dto4 = buildDto("user", tooLongEmail, "pass");

        for (FloraeUserRequestDTO dto : new FloraeUserRequestDTO[]{dto1, dto2, dto3, dto4}) {
            mockMvc.perform(post("/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email is not valid"));
        }
        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_invalidPassword_returnsBadRequest() throws Exception {
        FloraeUserRequestDTO dto1 = buildDto("user", "a@b.com", null);
        FloraeUserRequestDTO dto2 = buildDto("user", "a@b.com", "   ");
        StringBuilder sb = new StringBuilder();
        while (sb.toString().getBytes(StandardCharsets.UTF_8).length <= FloraeUser.MAX_PASSWORD_LENGTH) {
            sb.append("p");
        }
        FloraeUserRequestDTO dto3 = buildDto("user", "a@b.com", sb.toString());

        for (FloraeUserRequestDTO dto : new FloraeUserRequestDTO[]{dto1, dto2, dto3}) {
            mockMvc.perform(post("/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Password is not valid"));
        }
        verify(userService, never()).registerUser(any());
    }

    @Test
    void loginUser_invalidCredentials_returnsUnauthorized() throws Exception {
        FloraeUserRequestDTO dto = buildDto("user", "a@b.com", "pass");
        when(userService.isValid(dto)).thenReturn(false);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Invalid username or password"));

        verify(userService).isValid(dto);
        verify(userService, never()).getToken(any());
    }

    @Test
    void loginUser_validCredentials_returnsToken() throws Exception {
        FloraeUserRequestDTO dto = buildDto("user", "a@b.com", "pass");
        when(userService.isValid(dto)).thenReturn(true);
        when(userService.getToken("user")).thenReturn("JWT-TOKEN");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("JWT-TOKEN"));

        verify(userService).isValid(dto);
        verify(userService).getToken("user");
    }
}