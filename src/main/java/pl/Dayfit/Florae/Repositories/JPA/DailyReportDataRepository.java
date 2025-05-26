package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;

import java.util.List;

public interface DailyReportDataRepository extends JpaRepository<DailyReportData, Integer> {
    @Query("SELECT s " +
            "FROM DailyReportData s " +
            "WHERE s.floraeUser.username = :username")
    List<DailyReportData> findAllSensorReadingsByOwnerUsername(String username);
    DailyReportData findByFloraLink_Id(Integer floraLinkId);
}
