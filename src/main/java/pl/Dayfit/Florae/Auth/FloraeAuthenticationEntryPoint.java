package pl.Dayfit.Florae.Auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Implementation of the {@link AuthenticationEntryPoint} interface that acts as the entry point
 * for handling authentication errors in the application. It is typically responsible for sending
 * an HTTP 401 (Unauthorized) response when an unauthenticated user attempts to access a secured resource.
 * <p>
 * Key Responsibilities:
 * - Sets the HTTP response status to 401 (Unauthorized) when authentication fails.
 * - Sets the response content type to JSON and writes an error message in JSON format
 *   to indicate the authentication failure reason.
 * <p>
 * Usage Context:
 * - This class is usually registered as a bean and configured in security settings as the default
 *   authentication entry point for handling authentication errors.
 * <p>
 * Behavior:
 * - The {@code commence} method is triggered whenever an unauthenticated request
 *   is received by Spring Security, specifically in scenarios where authentication is required.
 * - Writes a standardized error response in JSON format containing the error message.
 * <p>
 * Dependency Configuration:
 * - Typically configured as a bean in the application's security configuration class
 *   (e.g., {@code SecurityConfiguration}) and used in conjunction with other security components.
 */
public class FloraeAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Authentication failed. " + authException.getMessage() + "\"}");
        response.getWriter().flush();
    }
}
