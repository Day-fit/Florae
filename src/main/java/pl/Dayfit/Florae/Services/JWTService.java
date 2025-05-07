package pl.Dayfit.Florae.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

/**
 * Service class responsible for handling JSON Web Tokens (JWT).
 * Provides methods for generating, validating, and extracting
 * claims from JWTs, leveraging a secret key for signature verification.

 * Annotations:
 * - {@code @Service}: Indicates that this class is a Spring service component.

 * Configuration:
 * - Requires a secret key to be defined in application properties
 *   under the key {@code jwt.secret}.
 */
@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String username){
        return Jwts.builder()
                .claims()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 30 * 1000))
                .and()
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey()
    {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token)
    {
        Date expiration = extractExpiration(token);

        if (expiration == null)
        {
            return true;
        }

        return expiration.before(new Date());
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

    public boolean validateToken(String token, String username) {
        String extractUsername = extractUsername(token);

        if (username == null){
            return false;
        }
        return extractUsername.equals(username) && !isTokenExpired(token);
    }
}
