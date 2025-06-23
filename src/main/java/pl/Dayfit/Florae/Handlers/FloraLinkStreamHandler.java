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
import pl.Dayfit.Florae.Entities.Redis.DailyReport;
import pl.Dayfit.Florae.Enums.ConnectionType;
import pl.Dayfit.Florae.Enums.SensorDataType;
import pl.Dayfit.Florae.Events.CurrentDataUploadedEvent;
import pl.Dayfit.Florae.Events.WebSocketConnectionEstablishedEvent;
import pl.Dayfit.Florae.Repositories.Redis.DailyReportRepository;
import pl.Dayfit.Florae.Services.PlantCacheService;

import java.io.EOFException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class FloraLinkStreamHandler implements WebSocketHandler {
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PlantCacheService plantCacheService;
    private final DailyReportRepository dailyReportRepository;
    private @Getter AtomicInteger activeFloraLinks = new AtomicInteger(0);

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        activeFloraLinks.incrementAndGet();
        eventPublisher.publishEvent(new WebSocketConnectionEstablishedEvent(session, ConnectionType.FLORALINK));
        log.trace("New connection established at {}", session.getLocalAddress());
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws JsonProcessingException {
        log.trace("Received message at {}: {}", session.getLocalAddress(), message.getPayload());

        if (!(message instanceof TextMessage textMessage))
        {
            log.debug("Received non-text message at {}. Message discarded.", session.getLocalAddress());
            return;
        }

        List<CurrentSensorDataDTO> sensorData = mapper.readValue(textMessage.getPayload(), new TypeReference<>() {});
        Authentication auth = (Authentication) session.getPrincipal();
        Plant plant = plantCacheService.getPlantById((Integer) session.getAttributes().get("id"));

        List<CurrentSensorDataDTO> filteredSensorData = sensorData.stream().filter(dto ->
        {
            try {
                SensorDataType.valueOf(dto.getType());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }).toList();

        if (auth == null)
        {
            log.warn("No authentication found for session {} at IP {}. Message discarded.", session.getId(), session.getLocalAddress());
            return;
        }

        if (plant == null) {
            log.warn("No plant found for session {} at IP {}. Message discarded.", session.getId(), session.getLocalAddress());
            return;
        }

        DailyReport report = dailyReportRepository.findDailyReportById(plant.getId().toString());

        if (report == null)
        {
            report = new DailyReport();
            report.setDailyReadings(new ConcurrentHashMap<>());
            report.setFloraLinkId(plant.getLinkedApiKey().getLinkedFloraLink().getId());
            report.setId(plant.getId().toString());
            report.setOwner(auth.getName());
        }

        Map<SensorDataType, DailyReport.DailyReadings> readings = report.getDailyReadings();

        if (readings == null)
        {
            readings = new ConcurrentHashMap<>();
        }

        if (filteredSensorData.size() != sensorData.size())
        {
            log.debug("Message have unknown sensor data at {}. Sending partial data.", session.getLocalAddress());
        }

        final Map<SensorDataType, DailyReport.DailyReadings> finalReadings = readings;
        filteredSensorData.forEach(dto ->
        {
            SensorDataType type = SensorDataType.valueOf(dto.getType());
            DailyReport.DailyReadings reading = finalReadings.get(type);

            if (reading == null)
            {
             reading = new DailyReport.DailyReadings();

             reading.setType(type);
             reading.setMaxTimestamp(Instant.now());
             reading.setMinTimestamp(Instant.now());
             reading.setAvgInitialTimestamp(Instant.now());
             reading.setAvgValue(dto.getValue());
             reading.setCount(1);
             reading.setMinValue(dto.getValue());
             reading.setMaxValue(dto.getValue());

             finalReadings.put(type, reading);
             return;
            }

            if (Duration.between(reading.getAvgInitialTimestamp(), Instant.now()).toHours() >= 24)
            {
                reading.setAvgInitialTimestamp(Instant.now());
                reading.setAvgValue(dto.getValue());
                reading.setCount(1);
            }

            if (reading.getMaxValue() == null || reading.getMaxValue() < dto.getValue() || Duration.between(reading.getMaxTimestamp(), Instant.now()).toDays() >= 24)
            {
                reading.setMaxValue(dto.getValue());
            }

            if (reading.getMinValue() == null || reading.getMinValue() > dto.getValue() || Duration.between(reading.getMinTimestamp(), Instant.now()).toDays() >= 24)
            {
                reading.setMinValue(dto.getValue());
            }

            Integer count = reading.getCount();

            if (count == 0)
            {
                log.warn("Count number in 24h report was 0, resetting to 1 to avoid division by zero. Session: {}", session.getLocalAddress());
                count = 1;
            }

            reading.setAvgValue((reading.getAvgValue() * count + dto.getValue()) / (count + 1));

            finalReadings.put(type, reading);
        });

        report.setDailyReadings(finalReadings);

        dailyReportRepository.save(report);
        eventPublisher.publishEvent(new CurrentDataUploadedEvent(filteredSensorData, auth, plant));
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
