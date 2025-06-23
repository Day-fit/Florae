package pl.Dayfit.Florae.Handlers.Channels;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Services.WebSockets.SessionService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelMessagingHandler implements MessageListener {
    private final SessionService sessionService;

    private void handleMessage(String message, String channel)
    {
        String identifier = null;
        WebSocketSession session = null;

        if (channel.startsWith("user."))
        {
            identifier = channel.replace("user.", "");
            session = sessionService.getSessionByUsername(identifier);
        }

        else if (channel.startsWith("floralink."))
        {
            identifier = channel.replace("floralink.", "");
            session = sessionService.getFloralinkSessionById(identifier);
        }

        if (identifier == null || session == null)
        {
            log.debug("Message received from invalid channel: {}", channel);
            return;
        }

        if (session.isOpen())
        {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException exception) {
                log.debug("Sending message to user {} failed: {}", identifier, exception.getMessage());
            }
        }
    }

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String messageContent = new String(message.getBody(), StandardCharsets.UTF_8);

        handleMessage(messageContent, channel);
    }
}
