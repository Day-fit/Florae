package pl.Dayfit.Florae.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.RefreshToken;
import pl.Dayfit.Florae.Repositories.FloraeUserRepository;
import pl.Dayfit.Florae.Repositories.RefreshTokenRepository;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

/**
 * Service class responsible for handling JSON Web Tokens (JWT).
 * Provides methods for generating, validating, and extracting
 * claims from JWTs, leveraging a secret key for signature verification.

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
    private final RefreshTokenRepository refreshTokenRepository;
    private final FloraeUserRepository floraeUserRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Generates a JWT token for the given username.
     * The token is signed with a secret key.
     *
     * @param username The username to be included in the token claims.
     * @param duration The duration of the token validity in minutes.
     * @return A JWT token for the given username.
     */
    public String generateAccessToken(String username, int duration){
        return Jwts.builder()
                .claims()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * duration))
                .and()
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * Generates a JWT refresh token for the given username and saves it to the database.
     * The token is signed with a secret key.
     *
     * @param username The username to be included in the token claims.
     * @param duration The duration of the token validity in days.
     */
    public String generateRefreshToken(String username, int duration)
    {
        Date expirationDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * duration);

        String refreshTokenHash = Jwts.builder()
                .claims()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationDate)
                .and()
                .signWith(getSecretKey())
                .compact();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenHash);
        refreshToken.setUser(floraeUserRepository.findByUsername(username));
        refreshToken.setExpiryDate(expirationDate);
        refreshToken.setIsRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return refreshTokenHash;
    }

    private SecretKey getSecretKey()
    {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
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
        try{
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException exception) {
            return null;
        }
    }

    /**
     * Used for validating a JWT access token.
     *
     * @param token The JWT access token to be validated.
     * @param username The username associated with the token.
     *
     * @return {@code true} if the token is valid and not expired, {@code false} otherwise.
     */
    public boolean validateAccessToken(String token, String username) {
        String extractUsername = extractUsername(token);

        if (extractUsername == null){
            return false;
        }

        return extractUsername.equals(username) && isTokenNotExpired(token);
    }

    /**
     * Used for validating a JWT refresh token.
     *
     * @param refreshToken The JWT refresh token to be validated.
     *
     * @return {@code true} if the token is valid and not expired, {@code false} otherwise.
     */
    public boolean validateRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken);

        if (token == null)
        {
            return false;
        }

        return isTokenNotExpired(refreshToken) && !token.getIsRevoked();
    }
}
