package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Events.UserConnectionClosed;
import pl.Dayfit.Florae.Events.UserConnectionEstablished;
import pl.Dayfit.Florae.Handlers.UserChannelHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageService {
    private final RedisMessageListenerContainer container;
    private final UserChannelHandler userChannelHandler;
    private final Map<String, ChannelTopic> topics = new ConcurrentHashMap<>();
    private final Map<String, MessageListenerAdapter> listeners = new ConcurrentHashMap<>();

    @EventListener
    public void register(UserConnectionEstablished event)
    {
        log.trace("Handling user connection registration id: {}", event.session().getId());

        WebSocketSession session = event.session();
        String username = (String) session.getAttributes().get("username");

        ChannelTopic topic = new ChannelTopic("user." + username);
        MessageListenerAdapter adapter = new MessageListenerAdapter(userChannelHandler, "handleMessage");

        container.addMessageListener(adapter, topic);

        topics.put(username, topic);
        listeners.put(username, adapter);
    }

    @EventListener
    public void unregister(UserConnectionClosed event)
    {
        log.trace("Handling user connection unregistration id: {}", event.session().getId());

        WebSocketSession session = event.session();
        String username = (String) session.getAttributes().get("username");

        container.removeMessageListener(listeners.get(username), topics.get(username));
        topics.remove(username);
        listeners.remove(username);
    }
}
