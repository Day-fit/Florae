package pl.Dayfit.Florae.Configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pl.Dayfit.Florae.Handlers.InputStreamHandler;
import pl.Dayfit.Florae.Handlers.OutputStreamHandler;
import pl.Dayfit.Florae.Handlers.WebSocketHandshakeHandler;
import pl.Dayfit.Florae.Interceptors.ApiHandshakeInterceptor;
import pl.Dayfit.Florae.Interceptors.UserHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final InputStreamHandler inputStreamHandler;
    private final OutputStreamHandler outputStreamHandler;
    private final WebSocketHandshakeHandler webSocketHandshakeHandler;
    private final ApiHandshakeInterceptor apiHandshakeInterceptor;
    private final UserHandshakeInterceptor userHandshakeInterceptor;

    @Value("${allowed.origins.patterns}")
    private String allowedOriginsPatterns;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(inputStreamHandler, "/ws/input-stream")
                .addInterceptors(apiHandshakeInterceptor)
                .setHandshakeHandler(webSocketHandshakeHandler)
                .setAllowedOriginPatterns("*"); //FloraLink can be any origin pattern

        registry
                .addHandler(outputStreamHandler, "/ws/output-stream")
                .addInterceptors(userHandshakeInterceptor)
                .setHandshakeHandler(webSocketHandshakeHandler)
                .setAllowedOriginPatterns(allowedOriginsPatterns.split(","));
    }
}
