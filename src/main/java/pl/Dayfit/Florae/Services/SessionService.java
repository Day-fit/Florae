package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Events.UserConnectionClosed;
import pl.Dayfit.Florae.Events.UserConnectionEstablished;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @EventListener
    public void registerSession(UserConnectionEstablished event) {
        WebSocketSession session = event.session();

        if (session == null) {
            log.warn("Session is null for event: {}", event);
            return;
        }

        if (session.getPrincipal() == null || session.getPrincipal().getName() == null)
        {
            log.warn("Session {} is null or principal name is null", session.getId());
        }

        sessions.put(session.getPrincipal().getName(), session);
    }


    @EventListener
    public void unregisterSession(UserConnectionClosed event)
    {
        WebSocketSession session = event.session();

        log.trace("Handling session unregister id: {}", session.getId());
        redisTemplate.delete((String) session.getAttributes().get("username"));
    }

    public WebSocketSession getSessionByUsername(String username)
    {
        return sessions.get(username);
    }
}
