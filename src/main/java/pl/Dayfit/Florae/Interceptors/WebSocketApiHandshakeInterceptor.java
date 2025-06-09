package pl.Dayfit.Florae.Interceptors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import pl.Dayfit.Florae.Auth.ApiKeyAuthenticationToken;
import pl.Dayfit.Florae.Entities.ApiKey;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyCacheService;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketApiHandshakeInterceptor implements HandshakeInterceptor {
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

        attributes.put("auth", new ApiKeyAuthenticationToken(apiKey));
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {
        if (exception != null)
        {
            log.trace("Aborting connection at {}, reason: {}", request.getLocalAddress(), exception.getMessage());
        }

        log.trace("Handshake complete at {}.", request.getLocalAddress());
    }
}
