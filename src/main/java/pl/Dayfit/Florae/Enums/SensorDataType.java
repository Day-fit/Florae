package pl.Dayfit.Florae.Enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SensorDataType {
    ENV_HUMIDITY("ENV_HUMIDITY"),
    ENV_TEMPERATURE("ENV_TEMPERATURE"),
    SOIL_MOISTURE("SOIL_MOISTURE"),
    LIGHT_LUX("LIGHT_LUX");

    private final String label;

    @Override
    public String toString()
    {
        return label;
    }
}
