package pl.Dayfit.Florae.Services.WebSockets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Enums.ConnectionType;
import pl.Dayfit.Florae.Events.WebSocketConnectionClosedEvent;
import pl.Dayfit.Florae.Events.WebSocketConnectionEstablishedEvent;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> floralinkSessions = new ConcurrentHashMap<>();

    @EventListener
    public void registerSession(WebSocketConnectionEstablishedEvent event) {
        WebSocketSession session = event.session();
        ConnectionType connectionType = event.connectionType();

        if (session == null) {
            log.warn("Session is null for event: {}", event);
            throw new IllegalStateException("Session is null for event: " + event);
        }

        if (connectionType == ConnectionType.FLORALINK)
        {
            Object potentialIdentifier = session.getAttributes().get("id");

            if (potentialIdentifier == null) {
                log.warn("Session {} does not have a linked plant id", session.getId());
                throw new IllegalStateException("Session does not have a id attribute");
            }

            if (!(potentialIdentifier instanceof Integer identifier))
            {
                log.warn("Session {} linked plant id is not an integer", session.getId());
                throw new IllegalStateException("Session attribute id is not an integer");
            }

            floralinkSessions.put(identifier.toString(), session);
            return;
        }

        Principal principal = session.getPrincipal();
        if (principal == null || principal.getName() == null)
        {
            log.warn("Session {} is null or principal name is null", session.getId());
            throw new IllegalStateException("Session principal is null or has no name");
        }

        userSessions.put(principal.getName(), session);
    }


    @EventListener
    public void unregisterSession(WebSocketConnectionClosedEvent event)
    {
        WebSocketSession session = event.session();

        log.trace("Handling session unregister id: {}", session.getId());

        redisTemplate.delete((String) session.getAttributes().get(event.connectionType() == ConnectionType.FLORALINK ? "linkedPlantId" : "username"));
    }

    public WebSocketSession getFloralinkSessionByLinkedPlantId(String linkedPlantId)
    {
        return floralinkSessions.get(linkedPlantId);
    }

    public WebSocketSession getSessionByUsername(String username)
    {
        return userSessions.get(username);
    }
}
