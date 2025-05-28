package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;

/**
 * Represents an ESP device entity in the system.
 * This class is used to map ESP-related data to the database.

 * Fields:
 * - {@code id}: The unique identifier for the ESP device,
 *   automatically generated.
 * - {@code name}: The name assigned to the ESP device,
 *   representing its logical identification.
 * - {@code macAddress}: The MAC address of the ESP device,
 *   stored in a format of fixed length 17 which follows the
 *   "XX:XX:XX:XX:XX:XX" pattern.

 * Annotations:
 * - {@code @Entity}: Marks this class as a JPA entity to map to the database.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations to automatically
 *   generate getter and setter methods for all fields.
 * - {@code @Id}: Denotes the primary key of the entity.
 * - {@code @GeneratedValue}: Indicates the primary key value is automatically
 *   generated.
 * - {@code @Column}: Configures the properties of the mapped database columns,
 *   e.g., whether the field is nullable or has specific length constraints.
 */
@Entity
@Getter
@Setter
public class FloraLink {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    private FloraeUser owner;

    @OneToOne(mappedBy = "floraLink", cascade = CascadeType.ALL)
    private DailyReportData dailyReportData;
}
