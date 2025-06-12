package pl.Dayfit.Florae.Configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pl.Dayfit.Florae.Handlers.Handshake.UserDetailsHandshakeHandler;
import pl.Dayfit.Florae.Handlers.InputStreamHandler;
import pl.Dayfit.Florae.Handlers.OutputStreamHandler;
import pl.Dayfit.Florae.Handlers.Handshake.ApiHandshakeHandler;
import pl.Dayfit.Florae.Interceptors.ApiHandshakeInterceptor;
import pl.Dayfit.Florae.Interceptors.UserDetailsHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final InputStreamHandler inputStreamHandler;
    private final OutputStreamHandler outputStreamHandler;
    private final ApiHandshakeHandler apiHandshakeHandler;
    private final ApiHandshakeInterceptor apiHandshakeInterceptor;
    private final UserDetailsHandshakeHandler userDetailsHandshakeHandler;
    private final UserDetailsHandshakeInterceptor userDetailsHandshakeInterceptor;

    @Value("${allowed.origins.patterns}")
    private String allowedOriginsPatterns;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(inputStreamHandler, "/ws/input-stream")
                .addInterceptors(apiHandshakeInterceptor)
                .setHandshakeHandler(apiHandshakeHandler)
                .setAllowedOriginPatterns("*"); //FloraLink can be any origin pattern

        registry
                .addHandler(outputStreamHandler, "/ws/output-stream")
                .addInterceptors(userDetailsHandshakeInterceptor)
                .setHandshakeHandler(userDetailsHandshakeHandler)
                .setAllowedOriginPatterns(allowedOriginsPatterns.split(","));
    }
}
