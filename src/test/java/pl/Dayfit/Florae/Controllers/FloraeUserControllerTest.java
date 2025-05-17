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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.Dayfit.Florae.DTOs.FloraeUserLoginDTO;
import pl.Dayfit.Florae.DTOs.FloraeUserRegisterDTO;
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
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    private FloraeUserRegisterDTO buildRegisterDto(String u, String e, String p) {
        FloraeUserRegisterDTO dto = new FloraeUserRegisterDTO();
        dto.setUsername(u);
        dto.setEmail(e);
        dto.setPassword(p);
        return dto;
    }

    private FloraeUserLoginDTO buildLoginDto(String u, String p)
    {
        FloraeUserLoginDTO dto = new FloraeUserLoginDTO();
        dto.setUsername(u);
        dto.setPassword(p);
        return dto;
    }

    @Test
    void registerUser_validData_returnsOk() throws Exception {
        FloraeUserRegisterDTO dto = buildRegisterDto("user1", "a@b.com", "pass123");
        doNothing().when(userService).registerUser(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully. Please check your email for verification."));

        verify(userService).registerUser(any());
    }

    @Test
    void registerUser_duplicateKey_returnsConflict() throws Exception {
        FloraeUserRegisterDTO dto = buildRegisterDto("user1", "a@b.com", "pass123");
        doThrow(new DuplicateKeyException("OK"))
                .when(userService).registerUser(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("OK"));

        verify(userService).registerUser(any());
    }

    @Test
    void registerUser_invalidUsername_returnsBadRequest() throws Exception {
        FloraeUserRegisterDTO dto1 = buildRegisterDto(null, "a@b.com", "pass");
        FloraeUserRegisterDTO dto2 = buildRegisterDto("   ", "a@b.com", "pass");
        FloraeUserRegisterDTO dto3 = buildRegisterDto("bad name!", "a@b.com", "pass");
        String tooLong = "a".repeat(FloraeUser.MAX_USERNAME_LENGTH + 1);
        FloraeUserRegisterDTO dto4 = buildRegisterDto(tooLong, "a@b.com", "pass");

        for (FloraeUserRegisterDTO dto : new FloraeUserRegisterDTO[]{dto1, dto2, dto3, dto4}) {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username is not valid"));
        }
        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_invalidEmail_returnsBadRequest() throws Exception {
        FloraeUserRegisterDTO dto1 = buildRegisterDto("user", null, "pass");
        FloraeUserRegisterDTO dto2 = buildRegisterDto("user", "   ", "pass");
        FloraeUserRegisterDTO dto3 = buildRegisterDto("user", "no-at-char", "pass");
        String tooLongEmail = "a".repeat(FloraeUser.MAX_EMAIL_LENGTH + 1) + "@x.com";
        FloraeUserRegisterDTO dto4 = buildRegisterDto("user", tooLongEmail, "pass");

        for (FloraeUserRegisterDTO dto : new FloraeUserRegisterDTO[]{dto1, dto2, dto3, dto4}) {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email is not valid"));
        }
        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_invalidPassword_returnsBadRequest() throws Exception {
        FloraeUserRegisterDTO dto1 = buildRegisterDto("user", "a@b.com", null);
        FloraeUserRegisterDTO dto2 = buildRegisterDto("user", "ab@b.com", "   ");
        StringBuilder sb = new StringBuilder();
        while (sb.toString().getBytes(StandardCharsets.UTF_8).length <= FloraeUser.MAX_PASSWORD_LENGTH) {
            sb.append("p");
        }
        FloraeUserRegisterDTO dto3 = buildRegisterDto("user", "abc@b.com", sb.toString());

        for (FloraeUserRegisterDTO dto : new FloraeUserRegisterDTO[]{dto1, dto2, dto3}) {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Password is not valid"));
        }
        verify(userService, never()).registerUser(any());
    }

    @Test
    void loginUser_invalidCredentials_returnsUnauthorized() throws Exception {
        FloraeUserLoginDTO dto = buildLoginDto("user", "pass");
        when(userService.isValid(dto)).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Invalid username or password"));

        verify(userService).isValid(dto);
        verify(userService, never()).getAccessToken(any());
    }

    @Test
    void loginUser_validCredentials_returnsToken() throws Exception {
        FloraeUserLoginDTO dto = buildLoginDto("user1", "pass1");
        when(userService.isValid(dto)).thenReturn(true);
        when(userService.getAccessToken("user1")).thenReturn("JWT-TOKEN");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("JWT-TOKEN"));

        verify(userService).isValid(dto);
        verify(userService).getAccessToken("user1");
    }
}