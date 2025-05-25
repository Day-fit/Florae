package pl.Dayfit.Florae.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationCandidate;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyService;

import java.io.IOException;

@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;
    private final String API_KEY_HEADER = "X-API-KEY";
    private final AuthenticationManager authenticationManager;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER).trim();

        if (!apiKeyService.isValidApiKey(apiKey))
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API key\"}");
            response.getWriter().flush();
            return;
        }

        if(!apiKeyService.isLinked(apiKey) && !request.getRequestURI().contains("/api/v1/floralink/connect-api"))
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"API key needs to be linked to a FloraLink before it can be used\"}");
            return;
        }

        Authentication authResult = authenticationManager.authenticate(new ApiKeyAuthenticationCandidate(apiKeyService.getApiKey(apiKey)));
        SecurityContextHolder.getContext().setAuthentication(authResult);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request)
    {
        return request.getHeader(API_KEY_HEADER) == null || (!request.getRequestURI().contains("/api/v1/floralink/upload") && !request.getRequestURI().contains("/api/v1/floralink/connect-api"));
    }
}
