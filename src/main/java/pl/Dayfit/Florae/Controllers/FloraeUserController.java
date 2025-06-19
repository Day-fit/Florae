package pl.Dayfit.Florae.Controllers;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.DTOs.FloraeUsers.FloraeUserLoginDTO;
import pl.Dayfit.Florae.DTOs.FloraeUsers.FloraeUserRegisterDTO;
import pl.Dayfit.Florae.DTOs.FloraeUsers.FloraeUserResponseDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserService;
import pl.Dayfit.Florae.Services.Auth.JWT.JWTService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
@Slf4j
@Controller
@RequiredArgsConstructor
public class FloraeUserController {
    private final FloraeUserService floraeUserService;
    private final FloraeUserCacheService floraeUserCacheService;
    private final JWTService jwtService;

    @Value("${florae.secured-cookies.enabled:false}")
    private boolean useSecuredCookies;
    @Value("${florae.cookie.policy:lax}")
    private String cookiePolicy;

    private static final String USERNAME_REGEX = "[a-zA-Z0-9_]+";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @PostConstruct
    private void init()
    {
        if (!useSecuredCookies)
        {
            log.warn("Running without secured cookies, should be used ONLY in dev environment");
        }
    }

   @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody FloraeUserRegisterDTO floraeUserRegisterDTO)
    {
        if (floraeUserRegisterDTO.getUsername() == null || floraeUserRegisterDTO.getUsername().isBlank() || !floraeUserRegisterDTO.getUsername().matches(USERNAME_REGEX) || floraeUserRegisterDTO.getUsername().length() > FloraeUser.MAX_USERNAME_LENGTH)
        {
           throw new IllegalArgumentException("Username is not valid");
        }

        if (floraeUserRegisterDTO.getEmail() == null || floraeUserRegisterDTO.getEmail().isBlank() || !floraeUserRegisterDTO.getEmail().matches(EMAIL_REGEX) || floraeUserRegisterDTO.getEmail().length() > FloraeUser.MAX_EMAIL_LENGTH)
        {
            throw new IllegalArgumentException("Email is not valid");
        }

        if (floraeUserRegisterDTO.getPassword() == null || floraeUserRegisterDTO.getPassword().isBlank() || floraeUserRegisterDTO.getPassword().getBytes(StandardCharsets.UTF_8).length > FloraeUser.MAX_PASSWORD_LENGTH)
        {
            throw new IllegalArgumentException("Password is not valid");
        }

        try {
            floraeUserService.registerUser(floraeUserRegisterDTO);
        } catch(DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "User registered successfully. Please check your email for verification."));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody FloraeUserLoginDTO floraeUserLoginDTO, HttpServletResponse response)
    {
        FloraeUser user;
        String username = floraeUserLoginDTO.getUsername();
        String email = floraeUserLoginDTO.getEmail();

        user = floraeUserLoginDTO.getUsername() == null? floraeUserCacheService.getFloraeUserByEmail(email) : floraeUserCacheService.getFloraeUser(username);

        if (user == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        if (!floraeUserService.isValid(floraeUserLoginDTO))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        if (floraeUserLoginDTO.getGenerateRefreshToken()) {
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", floraeUserService.getRefreshToken(user.getUsername()))
                    .path("/")
                    .sameSite(cookiePolicy)
                    .maxAge(60 * 60 * 24 * FloraeUserService.REFRESH_TOKEN_EXPIRATION_TIME)
                    .httpOnly(true)
                    .secure(useSecuredCookies)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        }

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", floraeUserService.generateAccessToken(user.getUsername()))
                .path("/")
                .maxAge(60 * FloraeUserService.ACCESS_TOKEN_EXPIRATION_TIME)
                .sameSite(cookiePolicy)
                .httpOnly(true)
                .secure(useSecuredCookies)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        return ResponseEntity.ok(Map.of("message", "User logged in successfully"));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No cookies found"));
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("");

        if (refreshToken.isBlank())
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token not found"));
        }

        if (!jwtService.validateRefreshToken(refreshToken))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
        }

        String newAccessToken = floraeUserService.refreshAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                .path("/")
                .maxAge(60 * FloraeUserService.ACCESS_TOKEN_EXPIRATION_TIME)
                .sameSite(cookiePolicy)
                .httpOnly(true)
                .secure(useSecuredCookies)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        return ResponseEntity.ok(Map.of("message","Access token refreshed successfully"));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logoutUser(@AuthenticationPrincipal UserPrincipal user, HttpServletRequest request, HttpServletResponse response)
    {
        Cookie[] cookies = request.getCookies();
        Cookie accessTokenCookie = null;
        Cookie refreshTokenCookie = null;

        if (cookies == null)
        {
            return ResponseEntity.ok(Map.of("message", "Already logged out"));
        }

        for (Cookie cookie : cookies)
        {
            if (accessTokenCookie != null && refreshTokenCookie != null)
            {
                break;
            }

            if (cookie.getName().equals("accessToken"))
            {
                accessTokenCookie = cookie;
                continue;
            }

            if (cookie.getName().equals("refreshToken"))
            {
                refreshTokenCookie = cookie;
            }
        }

        String accessToken = accessTokenCookie != null ? accessTokenCookie.getValue() : null;
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie.getValue() : null;

        if (refreshToken != null && !refreshToken.isBlank() && jwtService.validateRefreshToken(refreshToken)) {
            ResponseCookie deletedRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
                    .path("/")
                    .maxAge(0)
                    .sameSite(cookiePolicy)
                    .httpOnly(true)
                    .secure(useSecuredCookies)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, deletedRefreshTokenCookie.toString());

            jwtService.revokeToken(refreshToken);
        }
        if (accessToken != null && !accessToken.isBlank() && jwtService.validateAccessToken(accessToken, user.getUsername())) {
            ResponseCookie deletedAccessToken = ResponseCookie.from("accessToken", "")
                    .path("/")
                    .maxAge(0)
                    .sameSite(cookiePolicy)
                    .httpOnly(true)
                    .secure(useSecuredCookies)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, deletedAccessToken.toString());

            jwtService.revokeToken(accessToken);
        }

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping("/api/v1/get-user-data")
    public ResponseEntity<?> getUserData(@AuthenticationPrincipal UserPrincipal user)
    {
        if (user == null || user.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        FloraeUser floraeUser = floraeUserCacheService.getFloraeUser(user.getUsername());

        if (floraeUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(new FloraeUserResponseDTO(floraeUser.getUsername(), floraeUser.getEmail()));
    }
}
