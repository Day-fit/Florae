package pl.Dayfit.Florae.Interceptors;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.Services.Auth.JWT.FloraeUserCacheService;
import pl.Dayfit.Florae.Services.Auth.JWT.JWTService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsHandshakeInterceptor implements HandshakeInterceptor {
    private final JWTService jwtService;
    private final FloraeUserCacheService floraeUserCacheService;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws IllegalStateException,  AuthenticationCredentialsNotFoundException, BadCredentialsException{
        if (!(request instanceof ServletServerHttpRequest serverHttpRequest))
        {
            throw new IllegalStateException("Only HTTP requests are supported");
        }

        Cookie[] cookies = serverHttpRequest.getServletRequest().getCookies();

        if (cookies == null)
        {
            throw new AuthenticationCredentialsNotFoundException("No cookies found in request");
        }

        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("accessToken"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AccessToken cookie not found in request"))
                .getValue();

        String username = jwtService.extractUsername(accessToken);

        if (!jwtService.validateAccessToken(accessToken, username))
        {
            throw new BadCredentialsException("Invalid access token");
        }

        attributes.put("username", username);
        attributes.put("auth", new UserPrincipal(floraeUserCacheService.getFloraeUser(username)));

        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
        if (!(response instanceof ServletServerHttpResponse servletResponse))
        {
            log.debug("Response is not an instance of ServletServerHttpRequest, skipping...");
            return;
        }

        HttpServletResponse httpServletResponse = servletResponse.getServletResponse();

        if (exception != null) {
            try {
                httpServletResponse.sendError(401, "Failed to authorize request. reason: " + exception.getMessage());
            } catch (IOException e) {
                log.debug("Error while sending error response: {}, reason: {}", exception.getMessage(), e.getMessage());
            }

            log.debug("Aborting connection at {}, reason: {}", request.getRemoteAddress(), exception.getMessage());
            return;
        }

        log.trace("Handshake complete at {}.", request.getRemoteAddress());
    }
}
