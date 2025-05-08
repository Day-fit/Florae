// src/test/java/pl/Dayfit/Florae/Controllers/FloraeUserControllerTest.java
package pl.Dayfit.Florae.Controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.Dayfit.Florae.DTOs.FloraeUserRequestDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Services.FloraeUserService;

@WebMvcTest(FloraeUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class FloraeUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private FloraeUserService floraeUserService;

    @Nested
    @DisplayName("register endpoint")
    class RegisterEndpoint {

        @Test
        @DisplayName("returns successful registration when valid input provided")
        void returnsSuccessWhenInputIsValid() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("validUser");
            dto.setEmail("user@example.com");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User registered successfully"));
        }

        @Test
        @DisplayName("returns bad request when username is null")
        void returnsBadRequestWhenUsernameIsNull() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername(null);
            dto.setEmail("user@example.com");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username is not valid"));
        }

        @Test
        @DisplayName("returns bad request when username is blank")
        void returnsBadRequestWhenUsernameIsBlank() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("   ");
            dto.setEmail("user@example.com");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username is not valid"));
        }

        @Test
        @DisplayName("returns bad request when username contains illegal characters")
        void returnsBadRequestWhenUsernameNotMatchingPattern() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("not_valid![]'");
            dto.setEmail("user@example.com");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username is not valid"));
        }

        @Test
        @DisplayName("returns bad request when username is too long")
        void returnsBadRequestWhenUsernameTooLong() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("a".repeat(FloraeUser.MAX_USERNAME_LENGTH+1));
            dto.setEmail("user@example.com");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username is not valid"));
        }

        @Test
        @DisplayName("returns bad request when email is null")
        void returnsBadRequestWhenEmailIsNull() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("validUser");
            dto.setEmail(null);
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email is not valid"));
        }

        @Test
        @DisplayName("returns bad request when email format is invalid")
        void returnsBadRequestWhenEmailFormatInvalid() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("validUser");
            dto.setEmail("invalid-email");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email is not valid"));
        }

        @Test
        @DisplayName("returns bad request when email is too long")
        void returnsBadRequestWhenEmailTooLong() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();

            String emailDomain = "example.com";

            dto.setUsername("validUser");
            dto.setEmail("a".repeat(FloraeUser.MAX_EMAIL_LENGTH-emailDomain.length()) + "@example.com");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email is not valid"));
        }

        @Test
        @DisplayName("returns bad request when password is null")
        void returnsBadRequestWhenPasswordIsNull() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("validUser");
            dto.setEmail("user@example.com");
            dto.setPassword(null);

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Password is not valid"));
        }

        @Test
        @DisplayName("returns bad request when password is too long")
        void returnsBadRequestWhenPasswordTooLong() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("validUser");
            dto.setEmail("user@example.com");
            // UTF-8 - zakładamy, że przekraczamy 72 bajty
            dto.setPassword("a".repeat(73));

            String json = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Password is not valid"));
        }

        @Test
        @DisplayName("returns conflict when duplicate key exception is thrown")
        void returnsConflictOnDuplicateUser() throws Exception {
            FloraeUserRequestDTO dto = new FloraeUserRequestDTO();
            dto.setUsername("existingUser");
            dto.setEmail("user@example.com");
            dto.setPassword("validPassword");

            String json = objectMapper.writeValueAsString(dto);

            String exceptionMessage = "User already exists";
            doThrow(new DuplicateKeyException(exceptionMessage))
                    .when(floraeUserService).registerUser(any(FloraeUserRequestDTO.class));

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value(exceptionMessage));
        }
    }
}