package pl.Dayfit.Florae.Enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CommandType {
    WATERING("ADD_WATER"),
    ENABLE_BLE("ENABLE_BLE");

    private final String commandName;

    @Override
    public String toString() {
        return commandName;
    }
}
