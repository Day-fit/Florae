package pl.Dayfit.Florae.Events;

import org.springframework.web.socket.WebSocketSession;
import pl.Dayfit.Florae.Enums.ConnectionType;

public record WebSocketConnectionClosedEvent(WebSocketSession session, ConnectionType connectionType) {
}
