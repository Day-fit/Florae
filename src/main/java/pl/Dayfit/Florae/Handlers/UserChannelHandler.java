package pl.Dayfit.Florae.Handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Services.SessionService;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserChannelHandler {
    private final SessionService sessionService;

    @SuppressWarnings("unused")
    public void handleMessage(String message, String topic) //used in adapter
    {
        String username = topic.replace("user.", "");
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
}
