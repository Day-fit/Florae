package pl.Dayfit.Florae.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationCandidate;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyService;

import java.io.IOException;
import java.util.Collections;

/**
 * ApiKeyFilter is a custom implementation of {@link OncePerRequestFilter} that performs
 * API key-based authentication and authorization for incoming requests.
 * <p>
 * This filter inspects requests for a valid API key in the HTTP header "X-API-KEY".
 * It validates the API key using the provided {@link ApiKeyService} and manages
 * authentication within the application's security context.
 * <p>
 * Responsibilities:
 * - Extract the API key from the "X-API-KEY" header of the incoming request.
 * - Validate the provided API key through {@link ApiKeyService#isValidApiKey(String)}.
 * - Ensure the API key is linked (if required) through {@link ApiKeyService#isLinked(String)}.
 * - Authenticate valid API keys via the {@link AuthenticationManager}.
 * - Set the authenticated user into the {@link SecurityContextHolder}.
 * <p>
 * Behavior:
 * - For invalid API keys, responses are returned with HTTP status 401 (Unauthorized)
 *   along with a JSON error message.
 * - Requests without the "X-API-KEY" header, or that do not match certain URI conditions,
 *   are skipped through the {@link ApiKeyFilter#shouldNotFilter(HttpServletRequest)} method.
 * <p>
 * Methods:
 * - {@link ApiKeyFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)}:
 *   Processes the filter logic, including API key validation and authentication.
 * - {@link ApiKeyFilter#shouldNotFilter(HttpServletRequest)}:
 *   Determines whether the filter should be bypassed for specific requests.
 * <p>
 * Dependencies:
 * - This filter depends on {@link ApiKeyService} for API key validation and metadata access.
 * - It uses {@link AuthenticationManager} to perform authentication of API keys.
 */
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;
    private final String API_KEY_HEADER = "X-API-KEY";
    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String apiKey = getApiKeyHeader(request);

        if (apiKey == null || apiKey.isBlank()) {
            return;
        }

        apiKey = apiKey.trim();

        if (!apiKeyService.isValidApiKey(apiKey))
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API key\"}");
            response.getWriter().flush();
            return;
        }

        if(!apiKeyService.isLinked(apiKey) && !request.getRequestURI().contains("/api/v1/connect-api"))
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"API key needs to be linked to a FloraLink before it can be used\"}");
            return;
        }


        ApiKey apikey = apiKeyService.getApiKey(apiKey);

        Authentication authResult = authenticationManager.authenticate(new ApiKeyAuthenticationCandidate(apikey));
        SecurityContextHolder.getContext().setAuthentication(authResult);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String apiKey = getApiKeyHeader(request);
        boolean isApiKeyPresent = apiKey != null;
        boolean isMatchingPath = request.getRequestURI().contains("/api/v1/floralink/upload")
                || request.getRequestURI().contains("/api/v1/connect-api");

        return !(isApiKeyPresent && isMatchingPath);
    }

    private String getApiKeyHeader(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .filter(name -> name.equalsIgnoreCase(API_KEY_HEADER))
                .findFirst()
                .map(request::getHeader)
                .orElse(null);
    }
}
