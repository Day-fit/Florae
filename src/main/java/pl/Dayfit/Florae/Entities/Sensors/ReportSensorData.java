package pl.Dayfit.Florae.Entities.Sensors;

import pl.Dayfit.Florae.Entities.FloraLink;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportSensorData {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private ReportData sensorReadings;

    @Column(nullable = false)
    private String type;

    private Double minValue;
    private Instant minValueTimestamp;

    private Double maxValue;
    private Instant maxValueTimestamp;

    private Double averageValue;
}
