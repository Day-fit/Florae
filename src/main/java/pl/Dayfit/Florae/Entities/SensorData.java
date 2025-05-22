package pl.Dayfit.Florae.Entities;

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
public class SensorData {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private SensorReadings sensorReadings;

    @Column(nullable = false)
    private String type;

    private Double minValue;
    private Instant minValueTimestamp;

    private Double maxValue;
    private Instant maxValueTimestamp;

    private Double averageValue;
}
