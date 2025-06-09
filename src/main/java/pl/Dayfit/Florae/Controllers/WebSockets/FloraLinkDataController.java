package pl.Dayfit.Florae.Controllers.WebSockets;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorDataDTO;
import pl.Dayfit.Florae.DTOs.Sensors.CurrentSensorResponseDataDTO;
import pl.Dayfit.Florae.Entities.Sensors.CurrentSensorData;
import pl.Dayfit.Florae.Services.FloraLinkService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FloraLinkDataController {
    private FloraLinkService floraLinkService;

    @MessageMapping("/data-stream-input")
    @SendTo("/data-stream-output")
    public CurrentSensorResponseDataDTO getCurrentData(List<CurrentSensorDataDTO> dtoList)
    {
        floraLinkService.handleCurrentDataUpload(dtoList);
        return null; //zaślepka TODO: implement
    }
}
