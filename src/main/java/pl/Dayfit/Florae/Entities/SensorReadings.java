package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class SensorReadings {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    private FloraLink floraLink;

    @OneToMany(mappedBy = "sensorReadings", cascade = CascadeType.ALL)
    private List<SensorData> sensorDataList;
}
