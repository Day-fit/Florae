package pl.Dayfit.Florae.Services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {

    private JWTService jwtService;
    private final String validBase64Secret = "R6c5DHhqgLHzaeePvpMVxG8NlayobaFrZXc03LSwXAw=";

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JWTService();
        Field secretKeyField = JWTService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtService, validBase64Secret);
    }

    @Test
    void shouldGenerateValidTokenAndExtractUsername() {
        String username = "user1";
        String token = jwtService.generateToken(username);
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
        assertTrue(jwtService.validateToken(token, username));
    }

    @Test
    void shouldReturnFalseForTokenWithMismatchedUsername() {
        String username = "user1";
        String token = jwtService.generateToken(username);
        assertFalse(jwtService.validateToken(token, "differentUser"));
    }

    @Test
    void shouldReturnNullOnExtractUsernameForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertNull(jwtService.extractUsername(invalidToken));
        assertFalse(jwtService.validateToken(invalidToken, "user1"));
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
        assertFalse(jwtService.validateToken(expiredToken, username));
    }
}