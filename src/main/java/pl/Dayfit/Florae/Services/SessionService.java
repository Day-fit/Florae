package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Events.UserConnectionClosed;
import pl.Dayfit.Florae.Events.UserConnectionEstablished;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener
    public void registerSession(UserConnectionEstablished event)
    {
        WebSocketSession session = event.session();
        log.trace("Handling session register id: {}", session.getId());
        redisTemplate.opsForValue().set((String) session.getAttributes().get("username"), session);
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
        return (WebSocketSession) redisTemplate.opsForValue().get(username);
    }
}
