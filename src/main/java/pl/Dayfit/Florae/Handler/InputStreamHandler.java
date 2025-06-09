package pl.Dayfit.Florae.Handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorDataDTO;
import pl.Dayfit.Florae.Events.CurrentDataUploadedEvent;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class InputStreamHandler implements WebSocketHandler {
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private @Getter AtomicInteger activeFloraLinks = new AtomicInteger(0);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        activeFloraLinks.incrementAndGet();
        log.trace("New connection established at {}", session.getLocalAddress());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.trace("Received message at {}: {}", session.getLocalAddress(), message.getPayload());

        if (!(message instanceof TextMessage textMessage))
        {
            log.debug("Received non-text message at {}. Message discarded.", session.getLocalAddress());
            return;
        }

        try {
            List<CurrentSensorDataDTO> sensorData = mapper.readValue(textMessage.getPayload(), new TypeReference<>() {});
            Authentication auth = (Authentication) session.getPrincipal();

            if (auth == null)
            {
                log.warn("No authentication found for session {} at IP {}. Message discarded.", session.getId(), session.getLocalAddress());
                return;
            }

            eventPublisher.publishEvent(new CurrentDataUploadedEvent(sensorData, auth));
        } catch (JsonProcessingException exception){
            log.debug("Error while parsing incoming message: {}, reason: {}", textMessage.getPayload(), exception.getMessage());
        }
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        if (exception instanceof EOFException || exception.getMessage().contains("Broken pipe")) {
            log.debug("Client disconnected abruptly, session {}: {}", session.getId(), exception.getMessage());
            return;
        }

        if (session.isOpen())
        {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException e) {
                log.warn("Error while closing connection at {}: {}", session.getLocalAddress(), e.getMessage());
            }
            return;
        }

        log.error("Error while handling WebSocket connection at {}: {}", session.getLocalAddress(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        activeFloraLinks.decrementAndGet();
        log.trace("Connection closed at {}. Reason: {}", session.getLocalAddress(), closeStatus.getReason());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
