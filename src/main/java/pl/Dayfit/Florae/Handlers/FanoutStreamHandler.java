package pl.Dayfit.Florae.Handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Enums.ConnectionType;
import pl.Dayfit.Florae.Events.WebSocketConnectionClosedEvent;
import pl.Dayfit.Florae.Events.WebSocketConnectionEstablishedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class FanoutStreamHandler implements WebSocketHandler {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        log.trace("New connection established at {}, publishing event...", session.getLocalAddress());
        eventPublisher.publishEvent(new WebSocketConnectionEstablishedEvent(session, ConnectionType.USER));
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) {
        //This stream is out only, ignoring all incoming messages
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        //Ignored - no incoming messages, no transport errors
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
        log.trace("Connection closed at {}, publishing event...", session.getLocalAddress());
        eventPublisher.publishEvent(new WebSocketConnectionClosedEvent(session, ConnectionType.USER));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
