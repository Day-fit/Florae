package pl.Dayfit.Florae.Services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import pl.Dayfit.Florae.Repositories.JPA.BlacklistJwtTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.Dayfit.Florae.Services.Auth.JWT.JWTService;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JWTServiceTest {

    private JWTService jwtService;
    private final String validBase64Secret = "R6c5DHhqgLHzaeePvpMVxG8NlayobaFrZXc03LSwXAw="; //Test usage only

    @BeforeEach
    void setUp() throws Exception {
        BlacklistJwtTokenRepository mockRepository = mock(BlacklistJwtTokenRepository.class);
        jwtService = new JWTService(mockRepository);
        Field secretKeyField = JWTService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtService, validBase64Secret);
    }

    @Test
    void shouldGenerateValidTokenAndExtractUsername() {
        String username = "user1";
        String token = jwtService.generateAccessToken(username, 10);
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
        assertTrue(jwtService.validateAccessToken(token, username));
    }

    @Test
    void shouldReturnFalseForTokenWithMismatchedUsername() {
        String username = "user1";
        String token = jwtService.generateAccessToken(username, 10);
        assertFalse(jwtService.validateAccessToken(token, "differentUser"));
    }

    @Test
    void shouldReturnNullOnExtractUsernameForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertNull(jwtService.extractUsername(invalidToken));
        assertFalse(jwtService.validateAccessToken(invalidToken, "user1"));
    }

    @Test
    void shouldInvalidateExpiredToken() {
        String username = "expiredUser";
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(validBase64Secret));
        String expiredToken = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();
        assertNull(jwtService.extractUsername(expiredToken));
        assertFalse(jwtService.validateAccessToken(expiredToken, username));
    }
}