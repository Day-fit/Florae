package pl.Dayfit.Florae.Configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pl.Dayfit.Florae.Handler.InputStreamHandler;
import pl.Dayfit.Florae.Handler.WebSocketHandshakeHandler;
import pl.Dayfit.Florae.Interceptors.WebSocketApiHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final InputStreamHandler inputStreamHandler;
    private final WebSocketHandshakeHandler webSocketHandshakeHandler;
    private final WebSocketApiHandshakeInterceptor webSocketApiHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(inputStreamHandler, "/ws/input-stream")
                .addInterceptors(webSocketApiHandshakeInterceptor)
                .setHandshakeHandler(webSocketHandshakeHandler)
                .setAllowedOriginPatterns("*"); //FloraLink can be any origin pattern
    }
}
