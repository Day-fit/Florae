package pl.Dayfit.Florae.Entities.Sensors;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the current sensor data stored in Redis with a defined time-to-live (TTL).

 * This class serves as a transient container for sensor information, such as its type and value,
 * in the context of real-time data handling and distribution.
 * It is used in conjunction with {@link CurrentReportData} to represent a collection of sensor readings
 * associated with a specific user's linked device.

 * Fields:
 * - {@code id}: Unique identifier for the current sensor data entry.
 * - {@code type}: Indicates the type of sensor (e.g., temperature, humidity) providing the data.
 * - {@code value}: Represents the current reading from the sensor.

 * Annotations:
 * - {@code @RedisHash(value = "currentSensorData", timeToLive = 3600)}: Specifies that this
 *   class is stored as a Redis entity with a TTL of 3600 seconds.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations to automatically generate getter
 *   and setter methods for all fields.
 * - {@code @AllArgsConstructor}: Lombok annotation to automatically generate a constructor
 *   with all fields as parameters.
 * - {@code @Id}: Denotes the primary key of this Redis entity.
 */
@RedisHash(value = "currentSensorData", timeToLive = 3600)
@Getter
@Setter
@AllArgsConstructor
public class CurrentSensorData {
    @Id
    private String id;
    private String type;
    private Double value;
}
