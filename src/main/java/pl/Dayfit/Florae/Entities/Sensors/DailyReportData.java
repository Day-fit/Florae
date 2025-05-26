package pl.Dayfit.Florae.Entities.Sensors;

import pl.Dayfit.Florae.Entities.FloraLink;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.Dayfit.Florae.Entities.FloraeUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the daily report data in the system.
 * This entity is used to encapsulate sensor data recorded daily for a specific user and associated device.

 * Fields:
 * - {@code id}: The unique identifier for the daily report, automatically generated.
 * - {@code floraLink}: The associated FloraLink entity, representing the linked device, mapped as a one-to-one relationship.
 * - {@code floraeUser}: The associated user for whom this daily report is recorded, mapped as a many-to-one relationship.
 * - {@code sensorDataList}: A list of associated sensor data records (represented by {@link DailySensorData}),
 *   mapped as a one-to-many relationship with cascading and orphan removal enabled.

 * Annotations:
 * - {@code @Entity}: Indicates that this class is a JPA entity.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations to automatically generate getter and setter methods for all fields.
 * - {@code @Id}: Specifies the primary key of the entity.
 * - {@code @GeneratedValue}: Indicates that the primary key value is automatically generated.
 * - {@code @OneToOne}: Denotes a one-to-one relationship with {@code FloraLink}, with cascading operations enabled.
 * - {@code @ManyToOne}: Denotes a many-to-one relationship with {@code FloraeUser}.
 * - {@code @OneToMany}: Denotes a one-to-many relationship with {@code DailySensorData}, with cascading and orphan removal enabled.
 */
@Entity
@Getter
@Setter
public class DailyReportData {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    private FloraLink floraLink;

    @ManyToOne
    private FloraeUser floraeUser;

    @OneToMany(mappedBy = "sensorReadings", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailySensorData> sensorDataList = new ArrayList<>();
}
