package pl.Dayfit.Florae.Services.Auth.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Auth.FloraeKeyLocator;
import pl.Dayfit.Florae.Entities.BlacklistJwtToken;
import pl.Dayfit.Florae.Repositories.JPA.BlacklistJwtTokenRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class responsible for handling JSON Web Tokens (JWT).
 * Provides methods for generating, validating, and extracting
 * claims from JWTs, leveraging a secret key for signature verification.
 * <p>Please note that we are not encrypting the revoked tokens because it's pointless</p>

 * <p>Annotations:
 * <ul>
 *     <li>{@code @Service}: Indicates that this class is a Spring service component.</li>
 * </ul>

 * <p>Configuration:
 * <ul>
 *     <li>Requires a secret key to be defined in application properties under the key {@code jwt.secret}.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JWTService {
    private final BlacklistJwtTokenRepository blackListTokenRepository;
    private final FloraeKeyLocator floraeKeyLocator;
    private final SecretKeysService secretKeysService;

    /**
     * Generates a JWT token for the given username.
     * The token is signed with a secret key.
     *
     * @param username The username to be included in the token claims.
     * @param duration The duration of the token validity in minutes.
     * @return A JWT token for the given username.
     */
    public String generateAccessToken(String username, int duration){
        Map<String, Object> headers = new HashMap<>();
        headers.put("keyId", secretKeysService.getCurrentSecretKeyIndex());

        return Jwts.builder()
                .header()
                .add(headers)
                .and()
                .claims()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * duration))
                .and()
                .signWith(secretKeysService.getCurrentSecretKey())
                .compact();
    }

    /**
     * Generates a JWT refresh token for the given username and saves it to the database.
     * The token is signed with a current secret key.
     *
     * @param username The username to be included in the token claims.
     * @param duration The duration of the token validity in days.
     */
    public String generateRefreshToken(String username, int duration)
    {
        Date expirationDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * duration);
        Map<String, Object> headers = new HashMap<>();
        headers.put("keyId", secretKeysService.getCurrentSecretKeyIndex());

        return Jwts.builder()
                .header()
                .add(headers)
                .and()
                .claims()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expirationDate)
                .and()
                .signWith(secretKeysService.getCurrentSecretKey())
                .compact();
    }

    public void revokeToken(String token)
    {
        BlacklistJwtToken blackListJwtToken = new BlacklistJwtToken();
        blackListJwtToken.setToken(token);
        blackListJwtToken.setExpiryDate(extractExpiration(token));
        blackListTokenRepository.save(blackListJwtToken);
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private boolean isTokenNotExpired(String token)
    {
        Date expiration = extractExpiration(token);

        if (expiration == null)
        {
            return false;
        }

        return !expiration.before(new Date());
    }

    private Date extractExpiration(String token) throws ExpiredJwtException{
        return extractClaims(token, Claims::getExpiration);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) throws ExpiredJwtException{
        final Claims claims = extractAllClaims(token);

        if (claims == null){
            return null;
        }

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException {
        try {
            return Jwts.parser()
                    .keyLocator(floraeKeyLocator)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Used for validating a JWT access token.
     *
     * @param token The JWT access token to be validated.
     * @param username The username associated with the token.
     *
     * @return {@code true} if the token is valid, an owner is correct and not expired or blacklisted, {@code false} otherwise.
     */
    public boolean validateAccessToken(String token, String username) {
        String extractUsername = extractUsername(token);

        if (extractUsername == null){
            return false;
        }

        return extractUsername.equals(username) && isTokenNotExpired(token) && !blackListTokenRepository.existsByToken(token);
    }

    /**
     * Used for validating a JWT refresh token.
     *
     * @param refreshToken The JWT refresh token to be validated.
     *
     * @return {@code true} if the token is valid and not expired or blacklisted, {@code false} otherwise.
     */
    public boolean validateRefreshToken(String refreshToken) {
        return isTokenNotExpired(refreshToken) && !blackListTokenRepository.existsByToken(refreshToken);
    }
}
