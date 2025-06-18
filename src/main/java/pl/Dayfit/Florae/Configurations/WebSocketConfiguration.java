package pl.Dayfit.Florae.Configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.*;
import pl.Dayfit.Florae.Handlers.Handshake.UserDetailsHandshakeHandler;
import pl.Dayfit.Florae.Handlers.FloraLinkStreamHandler;
import pl.Dayfit.Florae.Handlers.FanoutStreamHandler;
import pl.Dayfit.Florae.Handlers.Handshake.ApiHandshakeHandler;
import pl.Dayfit.Florae.Interceptors.ApiHandshakeInterceptor;
import pl.Dayfit.Florae.Interceptors.UserDetailsHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final FloraLinkStreamHandler floraLinkStreamHandler;
    private final FanoutStreamHandler fanoutStreamHandler;
    private final ApiHandshakeHandler apiHandshakeHandler;
    private final ApiHandshakeInterceptor apiHandshakeInterceptor;
    private final UserDetailsHandshakeHandler userDetailsHandshakeHandler;
    private final UserDetailsHandshakeInterceptor userDetailsHandshakeInterceptor;

    @Value("${allowed.origins.patterns}")
    private String allowedOriginsPatterns;

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry
                .addHandler(floraLinkStreamHandler, "/ws/floralink")
                .addInterceptors(apiHandshakeInterceptor)
                .setHandshakeHandler(apiHandshakeHandler)
                .setAllowedOriginPatterns("*"); //FloraLink connection can be at any origin pattern

        registry
                .addHandler(fanoutStreamHandler, "/ws/fanout")
                .addInterceptors(userDetailsHandshakeInterceptor)
                .setHandshakeHandler(userDetailsHandshakeHandler)
                .setAllowedOriginPatterns(allowedOriginsPatterns.split(","));
    }
}
