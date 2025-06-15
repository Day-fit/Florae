package pl.Dayfit.Florae.Services.Auth.JWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Events.JWTRotationEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWTRotationService {
    private final ApplicationEventPublisher eventPublisher;
    private static final long SECRET_KEY_ROTATION_INTERVAL = TimeUnit.DAYS.toMillis(1); //One day

    @Scheduled(fixedRate = SECRET_KEY_ROTATION_INTERVAL)
    private void handleJwtSecretKeyRotation()
    {
        log.info("Handling JWT secret key rotation...");
        eventPublisher.publishEvent(new JWTRotationEvent());
        log.info("Rotation ended successfully.");
    }
}
