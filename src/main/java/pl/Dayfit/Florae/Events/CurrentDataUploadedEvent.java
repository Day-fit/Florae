package pl.Dayfit.Florae.Events;

import org.springframework.security.core.Authentication;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorDataDTO;

import java.util.List;

public record CurrentDataUploadedEvent(List<CurrentSensorDataDTO> data, Authentication authentication,
                                       pl.Dayfit.Florae.Entities.Plant plant) {
}
