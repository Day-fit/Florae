package pl.Dayfit.Florae.Services.WebSockets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Enums.ConnectionType;
import pl.Dayfit.Florae.Events.WebSocketConnectionClosedEvent;
import pl.Dayfit.Florae.Events.WebSocketConnectionEstablishedEvent;
import pl.Dayfit.Florae.Handlers.Channels.ChannelMessagingHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageService {
    private final RedisMessageListenerContainer container;
    private final ChannelMessagingHandler channelMessagingHandler;
    private final Map<String, ChannelTopic> topics = new ConcurrentHashMap<>();
    private final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();

    @EventListener
    public void register(WebSocketConnectionEstablishedEvent event)
    {
        log.trace("Handling user connection registration id: {}", event.session().getId());
        boolean isFloralink = event.connectionType() == ConnectionType.FLORALINK;

        WebSocketSession session = event.session();
        String identifier = session.getAttributes().get(isFloralink ? "id" : "username").toString();

        ChannelTopic topic = new ChannelTopic((isFloralink ? "floralink." : "user.") + identifier);

        MessageListener messageListener = channelMessagingHandler;

        container.addMessageListener(messageListener, topic);
        topics.put(identifier, topic);
        listeners.put(identifier, messageListener);
    }

    @EventListener
    public void unregister(WebSocketConnectionClosedEvent event)
    {
        log.trace("Handling user connection unregistration id: {}", event.session().getId());
        boolean isFloralink = event.connectionType() == ConnectionType.FLORALINK;

        WebSocketSession session = event.session();
        String identifier = (String) session.getAttributes().get(isFloralink ? "linkedPlantId" : "username");

        container.removeMessageListener(listeners.get(identifier), topics.get(identifier));
        topics.remove(identifier);
        listeners.remove(identifier);
    }
}
