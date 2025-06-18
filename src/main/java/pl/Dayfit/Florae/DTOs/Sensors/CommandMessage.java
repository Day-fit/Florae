package pl.Dayfit.Florae.DTOs.Sensors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.Dayfit.Florae.Enums.CommandType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommandMessage {
    CommandType type;
    double value;
}
