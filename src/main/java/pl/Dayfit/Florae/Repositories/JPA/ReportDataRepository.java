package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.Dayfit.Florae.Entities.Sensors.ReportData;

import java.util.List;

public interface ReportDataRepository extends JpaRepository<ReportData, Integer> {
    @Query("SELECT s " +
            "FROM ReportData s " +
            "WHERE s.floraLink.owner.username = :username")
    List<ReportData> findAllSensorReadingsByOwnerUsername(String username);
}
