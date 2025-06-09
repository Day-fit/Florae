package pl.Dayfit.Florae.Interceptors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import pl.Dayfit.Florae.Services.Auth.API.ApiKeyCacheService;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketsAuthInterceptor implements HandshakeInterceptor {
    private final @Getter AtomicInteger connectedFloraLinks = new AtomicInteger(0);
    private final ApiKeyCacheService apiKeyCacheService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        String apiKey = request.getHeaders().getFirst("X-API-KEY");

        if(apiKey == null || apiKey.isBlank())
        {
            throw new AuthenticationException("X-API-KEY header is missing or blank");
        }

        return apiKeyCacheService.getApiKey(apiKey) != null;
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
