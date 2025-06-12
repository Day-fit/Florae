package pl.Dayfit.Florae.Handlers.Handshake;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import pl.Dayfit.Florae.Auth.UserPrincipal;

import java.security.Principal;
import java.util.Map;

@Component
public class UserDetailsHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        UserDetails userDetails = (UserPrincipal) attributes.get("auth");

        if (userDetails == null)
        {
            return null;
        }

        return userDetails::getUsername;
    }
}
