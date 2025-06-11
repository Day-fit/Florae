package pl.Dayfit.Florae.Events;

import org.springframework.web.socket.WebSocketSession;

public record UserConnectionEstablished(WebSocketSession session) {
}
