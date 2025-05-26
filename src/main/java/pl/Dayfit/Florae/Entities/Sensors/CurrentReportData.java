package pl.Dayfit.Florae.Entities.Sensors;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

/**
 * Represents the current report data stored in Redis with a defined time-to-live (TTL).

 * This class is designed to store and manage real-time sensor data associated with a specific user's
 * linked device. It acts as a container for multiple sensor readings and facilitates quick and
 * efficient access to the latest data.

 * Fields:
 * - {@code id}: Unique identifier for the current report data entry.
 * - {@code floraLinkId}: Identifier representing the linked device associated with this report.
 * - {@code ownerUsername}: Username of the user associated with the current report data.
 * - {@code currentSensorDataList}: List of sensor readings represented by {@link CurrentSensorData}.

 * Annotations:
 * - {@code @RedisHash(value = "current_data", timeToLive = 3600)}: Specifies that this class is
 *   stored in Redis as an entity with a TTL of 3600 seconds.
 * - {@code @Id}: Denotes the primary key for this Redis entity.
 * - {@code @Indexed}: Indicates that the field will be indexed for faster search queries in Redis.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations to automatically generate getter and
 *   setter methods for all fields.
 * - {@code @AllArgsConstructor}: Lombok annotation to automatically generate a constructor with
 *   all fields as parameters.
 */
@RedisHash(value = "current_data", timeToLive = 3600)
@Getter
@Setter
@AllArgsConstructor
public class CurrentReportData {
    @Id
    private String id;

    @Indexed
    private String floraLinkId;

    @Indexed
    private String ownerUsername;
    private List<CurrentSensorData> currentSensorDataList;
}
