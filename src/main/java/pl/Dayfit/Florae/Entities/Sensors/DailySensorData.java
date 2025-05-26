package pl.Dayfit.Florae.Entities.Sensors;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents the daily sensor data associated with a specific daily report.
 * This entity holds various metrics such as type, minimum, maximum, and average values of the sensor's readings
 * along with their respective timestamps.

 * The relationship with {@link DailyReportData} establishes that this data is part of a daily report,
 * enabling structured and linked sensor data reporting for analysis.

 * Annotations:
 * - {@code @Entity}: Specifies that this class is a JPA entity.
 * - {@code @Getter} and {@code @Setter}: Automatically generates getter and setter methods for the fields.
 * - {@code @AllArgsConstructor}: Generates a constructor with all fields as parameters.
 * - {@code @NoArgsConstructor}: Generates a default constructor with no parameters.
 * - {@code @Id}: Indicates the primary key for this entity.
 * - {@code @GeneratedValue}: Specifies that the primary key will be generated automatically.
 * - {@code @ManyToOne}: Defines a many-to-one relationship with the {@link DailyReportData} class.
 * - {@code @Column(nullable = false)}: Ensures that the 'type' field cannot be null in the database.

 * Fields:
 * - {@code id}: Unique identifier for the entity.
 * - {@code sensorReadings}: Reference to the associated daily report data.
 * - {@code type}: Type of the sensor providing the specific readings (e.g., temperature, humidity).
 * - {@code minValue}: Minimum value recorded by the sensor.
 * - {@code minValueTimestamp}: Timestamp of when the minimum value was recorded.
 * - {@code maxValue}: Maximum value recorded by the sensor.
 * - {@code maxValueTimestamp}: Timestamp of when the maximum value was recorded.
 * - {@code averageValue}: Average value of the sensor readings over the day.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailySensorData {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private DailyReportData sensorReadings;

    @Column(nullable = false)
    private String type;

    private Double minValue;
    private Instant minValueTimestamp;

    private Double maxValue;
    private Instant maxValueTimestamp;

    private Double averageValue;
}
