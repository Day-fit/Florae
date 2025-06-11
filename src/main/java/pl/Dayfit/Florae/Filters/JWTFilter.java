package pl.Dayfit.Florae.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.Dayfit.Florae.Services.Auth.JWT.JWTService;

import java.io.IOException;
import java.util.Arrays;


/**
 * A custom filter that intercepts HTTP requests to validate JSON Web Tokens (JWT)
 * included in cookies for authentication and authorization purposes. It extends
 * the {@code OncePerRequestFilter} class to ensure that the filter is executed once
 * per request in a web application.
 * <p>
 * Responsibilities include:
 * - Verifying the presence and validity of the "accessToken" cookie in incoming requests.
 * - Extracting user details from a valid JWT and setting up the security context.
 * - Enforcing security by rejecting requests with invalid or missing authentication details.
 * - Allowing unfiltered access for non-protected paths or requests with a valid API key.
 * <p>
 * Dependencies:
 * - {@code JWTService}: Provides methods for JWT validation and extraction of token claims.
 * - {@code UserDetailsService}: Loads user-specific data from the application's data store.
 * <p>
 * Annotations:
 * - {@code @Component}: Indicates that this class is a Spring-managed component.
 * - {@code @RequiredArgsConstructor}: Automates the construction of instances with required dependencies.
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${security.protected-paths}")
    private String PROTECTED_PATHS;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, BadCredentialsException {
        if (hasValidAccessTokenCookie(request))
        {
            handleCookieAccess(request);
            filterChain.doFilter(request, response);
            return;
        }

        throw new BadCredentialsException("Credentials are invalid");
    }

    private boolean hasValidAccessTokenCookie(HttpServletRequest request) throws AuthenticationException
    {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthenticationCredentialsNotFoundException("No cookies found in request");
        }
        String accessToken;
        Cookie accessTokenCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("accessToken")).findFirst().orElse(null);

        if(accessTokenCookie == null)
        {
            throw new AuthenticationCredentialsNotFoundException("No accessToken cookie found in request");
        }

        accessToken = accessTokenCookie.getValue();
        return jwtService.validateAccessToken(accessToken, jwtService.extractUsername(accessToken));
    }

    private void handleCookieAccess(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        String accessToken = Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals("accessToken"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Cookie not found in request")) //Should never happen
                        .getValue();

        String username = jwtService.extractUsername(accessToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request)
    {
        String uri = request.getRequestURI();
        boolean hasValidApiKey = request.getHeader("X-API-KEY") != null
                && (uri.contains("/api/v1/floralink/upload") || uri.contains("/api/v1/floralink/connect-api"));

        boolean isProtectedPath = Arrays.stream(PROTECTED_PATHS.split(","))
                .map(String::trim)
                .anyMatch(uri::startsWith);

        return hasValidApiKey || !isProtectedPath;
    }
}
