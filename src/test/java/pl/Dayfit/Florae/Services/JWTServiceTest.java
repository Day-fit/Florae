package pl.Dayfit.Florae.Services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Header;
import org.mockito.Mockito;
import pl.Dayfit.Florae.Auth.FloraeKeyLocator;
import pl.Dayfit.Florae.Repositories.JPA.BlacklistJwtTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.Dayfit.Florae.Services.Auth.JWT.JWTService;
import pl.Dayfit.Florae.Services.Auth.JWT.SecretKeysService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JWTServiceTest {
    private JWTService jwtService;
    private final SecretKeysService secretKeysService = new SecretKeysService();

    @BeforeEach
    void setUp() {
        BlacklistJwtTokenRepository mockRepository = mock(BlacklistJwtTokenRepository.class);
        FloraeKeyLocator floraeKeyLocator = mock(FloraeKeyLocator.class);

        //Test usage only
        String validBase64Secret = "R6c5DHhqgLHzaeePvpMVxG8NlayobaFrZXc03LSwXAw=";
        ConcurrentMap<Integer, String> newSecretKeys = new ConcurrentHashMap<>(Map.of(0, validBase64Secret));
        secretKeysService.setSecretKeys(newSecretKeys);
        secretKeysService.setCurrentSecretKeyIndex(0);

        Mockito.when(floraeKeyLocator.locate(Mockito.any(Header.class)))
                .thenAnswer(invocation -> secretKeysService.getCurrentSecretKey());

        jwtService = new JWTService(mockRepository , floraeKeyLocator, secretKeysService);
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
        Map<String, Object> headers = new HashMap<>();
        headers.put("keyId", 0);

        String expiredToken = Jwts.builder()
                .header()
                .add(headers)
                .and()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(secretKeysService.getCurrentSecretKey())
                .compact();
        assertNull(jwtService.extractUsername(expiredToken));
        assertFalse(jwtService.validateAccessToken(expiredToken, username));
    }
}
