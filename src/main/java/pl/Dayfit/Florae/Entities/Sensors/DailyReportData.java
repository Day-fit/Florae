package pl.Dayfit.Florae.Entities.Sensors;

import pl.Dayfit.Florae.Entities.FloraLink;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.Dayfit.Florae.Entities.FloraeUser;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "sensorReadings", cascade = CascadeType.ALL)
    private List<DailySensorData> sensorDataList = new ArrayList<>();
}
