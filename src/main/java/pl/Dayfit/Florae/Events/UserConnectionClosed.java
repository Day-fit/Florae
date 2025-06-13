package pl.Dayfit.Florae.Events;

import org.springframework.web.socket.WebSocketSession;

public record UserConnectionClosed(WebSocketSession session) {
}
