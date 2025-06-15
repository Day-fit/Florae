package pl.Dayfit.Florae.Services.Auth.JWT;

import io.jsonwebtoken.security.Keys;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Events.JWTRotationEvent;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service responsible for managing the SecretKeys
 */
@Slf4j
@Service
public class SecretKeysService {
    private final AtomicInteger currentSecretKeyIndex = new AtomicInteger(-1);
    private @Setter ConcurrentMap<Integer, String> secretKeys = new ConcurrentHashMap<>();

    /**
     * Generates a new SecretKey when the `JWTRotationEvent` is being published
     * @param event the `JWTRotationEvent` event instance
     */
    @EventListener
    @SuppressWarnings("unused")
    private void handleSecretKeyGeneration(JWTRotationEvent event)
    {
        int MAX_SECRET_KEYS = FloraeUserService.REFRESH_TOKEN_EXPIRATION_TIME + 1;
        int index = (currentSecretKeyIndex.get() + 1) % MAX_SECRET_KEYS;

        try {
            KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
            secretKeys.put(index, Base64.getEncoder().encodeToString(generator.generateKey().getEncoded()));
            currentSecretKeyIndex.set(index);
        } catch (NoSuchAlgorithmException exception) {
            log.error("A runtime error occurred while generating a secret key for index {}: {}", index, exception.getMessage());
            throw new RuntimeException("Failed to generate secret key for index " + index, exception);
        }
    }

    public SecretKey getCurrentSecretKey()
    {
        int index = currentSecretKeyIndex.get();
        if (index < 0)
        {
            return null;
        }

        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeys.get(index)));
    }

    public int getCurrentSecretKeyIndex()
    {
        return currentSecretKeyIndex.get();
    }

    public SecretKey getCurrentSecret(Integer index) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeys.get(index));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public void setCurrentSecretKeyIndex(int index)
    {
        currentSecretKeyIndex.set(index);
    }
}
