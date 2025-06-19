package pl.Dayfit.Florae.DTOs.Sensors.Commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.Dayfit.Florae.Enums.CommandType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommandMessage extends Command{
    public CommandMessage(CommandType type, double value)
    {
        super(type);
        this.value = value;
    }

    private double value;
}
