package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.Dayfit.Florae.DTOs.FloraeUserLoginDTO;
import pl.Dayfit.Florae.DTOs.FloraeUserRegisterDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Services.FloraeUserService;
import pl.Dayfit.Florae.Services.JWTService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * Controller for handling user-related operations in the Florae system.
 * Provides endpoints for user registration and authentication.

 * Endpoints:
 * - /auth/register: Registers a new user with provided details.
 * - /auth/login: Authenticates a user and provides a JSON Web Token (JWT) upon successful login.
 * - /auth/refresh: Refreshes an existing JWT token using a refresh token.
 * - /auth/logout: Handles revoking JWT refresh token

 * Dependencies:
 * - {@code FloraeUserService}: Service layer responsible for handling user operations such as registration,
 *   validation, and token generation.
 * - {@code JWTService}: Service layer responsible for validation, revoking tokens.

 * Validations:
 * - Ensures that the username, email, and password provided during registration meet the specified format
 *   and length requirements.

 * Error Handling:
 * - Returns appropriate HTTP status codes and error messages for validation failures and duplicate entries.
 */
@Controller
@RequiredArgsConstructor
public class FloraeUserController {
    private final FloraeUserService floraeUserService;
    private final JWTService jwtService;

    private static final String USERNAME_REGEX = "[a-zA-Z0-9_]+";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody FloraeUserRegisterDTO floraeUserRegisterDTO)
    {
        if (floraeUserRegisterDTO.getUsername() == null || floraeUserRegisterDTO.getUsername().isBlank() || !floraeUserRegisterDTO.getUsername().matches(USERNAME_REGEX) || floraeUserRegisterDTO.getUsername().length() > FloraeUser.MAX_USERNAME_LENGTH)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Username is not valid"));
        }

        if (floraeUserRegisterDTO.getEmail() == null || floraeUserRegisterDTO.getEmail().isBlank() || !floraeUserRegisterDTO.getEmail().matches(EMAIL_REGEX) || floraeUserRegisterDTO.getEmail().length() > FloraeUser.MAX_EMAIL_LENGTH)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email is not valid"));
        }

        if (floraeUserRegisterDTO.getPassword() == null || floraeUserRegisterDTO.getPassword().isBlank() || floraeUserRegisterDTO.getPassword().getBytes(StandardCharsets.UTF_8).length > FloraeUser.MAX_PASSWORD_LENGTH)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Password is not valid"));
        }

        try {
            floraeUserService.registerUser(floraeUserRegisterDTO);
        } catch(DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "User registered successfully. Please check your email for verification."));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody FloraeUserLoginDTO floraeUserLoginDTO)
    {
        Map<String, String> response = new HashMap<>();

        if (!floraeUserService.isValid(floraeUserLoginDTO)) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (floraeUserLoginDTO.getGenerateRefreshToken()) {
            response.put("refreshToken", floraeUserService.getRefreshToken(floraeUserLoginDTO.getUsername()));
        }

        response.put("accessToken", floraeUserService.getAccessToken(floraeUserLoginDTO.getUsername()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshToken)
    {
        if (refreshToken == null || refreshToken.get("refreshToken") == null || refreshToken.get("refreshToken").isBlank())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Refresh token is empty or invalid"));
        }

        String accessToken = floraeUserService.getAccessTokenFromRefreshToken(refreshToken.get("refreshToken"));

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
        }

        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logoutUser(@RequestBody String refreshToken)
    {
        if (refreshToken == null || refreshToken.isBlank())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Refresh token is empty or invalid"));
        }

        if (!jwtService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
        }

        jwtService.revokeToken(refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
