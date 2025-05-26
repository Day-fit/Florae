package pl.Dayfit.Florae.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.Dayfit.Florae.Services.Auth.JWT.JWTService;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * A filter that intercepts HTTP requests to validate and process JSON Web Tokens (JWT)
 * for authentication purposes.
 * This filter extends the {@link OncePerRequestFilter} to ensure it is executed once per request.
 * It extracts the Authorization header, validates the provided JWT, and sets the user
 * authentication context if the token is valid. This enables stateless authentication for the
 * application by leveraging JWTs.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>{@link JWTService}: Provides methods to extract and validate the JWT.</li>
 *   <li>{@link UserDetailsService}: Loads user-specific data for authentication.</li>
 * </ul>
 *
 * <p>Key Functionality:</p>
 * <ul>
 *   <li>Extracts the JWT from the "Authorization" header of incoming HTTP requests.</li>
 *   <li>Validates the JWT using the {@code JWTService}.</li>
 *   <li>If valid, retrieves user details via {@code UserDetailsService} and sets up authentication
 *       in the {@link SecurityContextHolder}.</li>
 *   <li>Passes the request and response objects to the next filter in the chain if validation succeeds.</li>
 *   <li>Skips filtering for requests where authentication is already present or for specific request paths.</li>
 * </ul>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>{@code @Component}: Marks this class as a Spring-managed component.</li>
 *   <li>{@code @RequiredArgsConstructor}: Generates constructor for injecting final fields.</li>
 * </ul>
 *
 * <p>Integration:</p>
 * <ul>
 *   <li>Works in conjunction with {@link ApiKeyFilter} in the security filter chain.</li>
 *   <li>Part of the stateless authentication mechanism using Spring Security.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${security.public-paths}")
    private List<String> PUBLIC_PATHS;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        String token;
        String username;

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid JWT (missing Bearer prefix) or missing Authorization header. Please provide a valid JWT in the Authorization header.");
        }

        token = authHeader.substring(7).trim();
        username = jwtService.extractUsername(token);

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            throw new BadRequestException("Invalid JWT (missing username or already authenticated). Please provide a valid JWT in the Authorization header.");
        }

        UserDetails userDetails;

        try{
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            logger.error("User not found with username: " + username);
            throw new AccessDeniedException("User not found with username: " + username);
        }

        if (!jwtService.validateAccessToken(token, username)){
            throw new AccessDeniedException("Invalid JWT (expired or invalid signature). Please provide a valid JWT in the Authorization header.");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request)
    {
        return (request.getHeader("X-API-KEY") != null && (request.getRequestURI().contains("/api/v1/floralink/upload") || request.getRequestURI().contains("/api/v1/floralink/connect-api"))) || PUBLIC_PATHS.stream().anyMatch(request.getRequestURI()::equalsIgnoreCase);
    }
}
