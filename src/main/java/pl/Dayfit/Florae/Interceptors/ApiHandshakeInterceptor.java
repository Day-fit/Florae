package pl.Dayfit.Florae.Interceptors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationToken;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyCacheService;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiHandshakeInterceptor implements HandshakeInterceptor {
    private final ApiKeyCacheService apiKeyCacheService;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        if (!(request instanceof ServletServerHttpRequest serverRequest))
        {
            return false;
        }

        String apiKeyRawValue = serverRequest.getHeaders().getFirst("X-API-KEY");

        if(apiKeyRawValue == null || apiKeyRawValue.isBlank())
        {
            throw new AuthenticationException("X-API-KEY header is missing or blank");
        }

        ApiKey apiKey = apiKeyCacheService.getApiKey(apiKeyRawValue);

        if (apiKey == null)
        {
            throw new AuthenticationException("Invalid API key");
        }

        if(apiKey.getLinkedFloraLink() == null)
        {
            throw new AuthenticationException("API key is not linked to any FloraLink");
        }

        attributes.put("auth", new ApiKeyAuthenticationToken(apiKey));
        attributes.put("owner", apiKey.getFloraeUser().getUsername());
        attributes.put("id", apiKey.getLinkedFloraLink().getId());

        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {

        if (!(response instanceof ServletServerHttpResponse servletResponse))
        {
            log.debug("Response is not an instance of ServletServerHttpRequest, skipping...");
            return;
        }

        if (exception != null)
        {
            log.trace("Aborting connection at {}, reason: {}", request.getLocalAddress(), exception.getMessage());

            try {
                servletResponse.getServletResponse().sendError(400, "Handshake failed: " + exception.getMessage());
            } catch(IOException ex){
                log.debug("Failed to send error response after handshake failure: {}", ex.getMessage(), ex);
            }
        }

        log.trace("Handshake complete at {}.", request.getLocalAddress());
    }
}
