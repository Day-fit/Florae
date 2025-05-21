package pl.Dayfit.Florae.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.Dayfit.Florae.Entities.SensorReadings;

import java.util.List;

public interface SensorReadingsRepository extends JpaRepository<SensorReadings, Integer> {
    @Query("SELECT s " +
            "FROM SensorReadings s " +
            "WHERE s.floraLink.owner.username = :username")
    List<SensorReadings> findAllSensorReadingsByOwnerUsername(String username);
}
