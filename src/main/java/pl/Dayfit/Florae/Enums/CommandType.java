package pl.Dayfit.Florae.Enums;

public enum CommandType {
    WATERING("ADD_WATER"),
    ENABLE_BLE("ENABLE_BLE");

    private final String commandName;

    CommandType(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String toString() {
        return commandName;
    }
}
