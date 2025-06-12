package pl.Dayfit.Florae.Handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Services.SessionService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserChannelHandler implements MessageListener {
    private final SessionService sessionService;

    private void handleMessage(String message, String channel)
    {
        log.info("message: {}, channel {}", message, channel);
        String username = channel.replace("user.", "");
        WebSocketSession session = sessionService.getSessionByUsername(username);

        if (session != null && session.isOpen())
        {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException exception) {
                log.debug("Sending message to user {} failed: {}", username, exception.getMessage());
            }
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String messageContent = new String(message.getBody(), StandardCharsets.UTF_8);

        handleMessage(messageContent, channel);
    }
}
