package pl.Dayfit.Florae.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.Dayfit.Florae.Services.JWTService;

import java.io.IOException;
import java.util.List;

/**
 * A filter that intercepts HTTP requests to validate and process JSON Web Tokens (JWT)
 * for authentication purposes.
 * This filter extends the {@link OncePerRequestFilter} to ensure it is executed once per request.
 * It extracts the Authorization header, validates the provided JWT, and sets the user
 * authentication context if the token is valid. This enables stateless authentication for the
 * application by leveraging JWTs.
 * Dependencies:
 * - {@link JWTService}: Provides methods to extract and validate the JWT.
 * - {@link UserDetailsService}: Loads user-specific data for authentication.
 * Key Functionality:
 * - Extracts the JWT from the "Authorization" header of incoming HTTP requests.
 * - Validates the JWT using the {@code JWTService}.
 * - If valid, retrieves user details via {@code UserDetailsService} and sets up authentication
 *   in the {@link SecurityContextHolder}.
 * - Passes the request and response objects to the next filter in the chain if validation succeeds.
 * Annotations:
 * - {@code @Component}: Marks this class as a Spring-managed component.
 * - {@code @RequiredArgsConstructor}: Generates constructor for injecting final fields.
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${security.public-paths}")
    private final List<String> PUBLIC_PATHS;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        String token;
        String username;

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or missing JWT\"}");
            response.getWriter().flush();
            return;
        }

        token = authHeader.substring(7).trim();
        username = jwtService.extractUsername(token);

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid JWT (missing username or already authenticated user)\"}");
            response.getWriter().flush();
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.validateAccessToken(token, username)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid JWT (failed validation)\"}");
            response.getWriter().flush();
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request)
    {
        return (request.getHeader("X-Api-Key") != null && request.getRequestURI().contains("/api/v1/floralink")) || PUBLIC_PATHS.stream().anyMatch(request.getRequestURI()::equalsIgnoreCase);
    }
}
