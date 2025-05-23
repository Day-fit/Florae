package pl.Dayfit.Florae.Entities.Sensors;

import pl.Dayfit.Florae.Entities.FloraLink;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ReportData {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    private FloraLink floraLink;

    @OneToMany(mappedBy = "sensorReadings", cascade = CascadeType.ALL)
    private List<ReportSensorData> sensorDataList = new ArrayList<>();
}
