package pl.Dayfit.Florae.Handlers;

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
import pl.Dayfit.Florae.Entities.Plant;
import pl.Dayfit.Florae.Enums.ConnectionType;
import pl.Dayfit.Florae.Events.CurrentDataUploadedEvent;
import pl.Dayfit.Florae.Events.WebSocketConnectionEstablishedEvent;
import pl.Dayfit.Florae.Services.PlantCacheService;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class FloraLinkStreamHandler implements WebSocketHandler {
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PlantCacheService plantCacheService;
    private @Getter AtomicInteger activeFloraLinks = new AtomicInteger(0);

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        activeFloraLinks.incrementAndGet();
        eventPublisher.publishEvent(new WebSocketConnectionEstablishedEvent(session, ConnectionType.FLORALINK));
        log.trace("New connection established at {}", session.getLocalAddress());
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) {
        log.trace("Received message at {}: {}", session.getLocalAddress(), message.getPayload());

        if (!(message instanceof TextMessage textMessage))
        {
            log.debug("Received non-text message at {}. Message discarded.", session.getLocalAddress());
            return;
        }

        try {
            List<CurrentSensorDataDTO> sensorData = mapper.readValue(textMessage.getPayload(), new TypeReference<>() {});
            Authentication auth = (Authentication) session.getPrincipal();
            Plant plant = plantCacheService.getPlantById((Integer) session.getAttributes().get("id"));

            if (auth == null)
            {
                log.warn("No authentication found for session {} at IP {}. Message discarded.", session.getId(), session.getLocalAddress());
                return;
            }

            if (plant == null) {
                log.warn("No plant found for session {} at IP {}. Message discarded.", session.getId(), session.getLocalAddress());
                return;
            }

            eventPublisher.publishEvent(new CurrentDataUploadedEvent(sensorData, auth, plant));
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
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
        activeFloraLinks.decrementAndGet();
        eventPublisher.publishEvent(new WebSocketConnectionEstablishedEvent(session, ConnectionType.FLORALINK));
        log.trace("Connection closed at {}. Reason: {}", session.getLocalAddress(), closeStatus.getReason());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
