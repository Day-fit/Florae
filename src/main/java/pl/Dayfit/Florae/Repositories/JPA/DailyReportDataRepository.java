package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.Dayfit.Florae.Entities.Sensors.DailyReportData;

import java.util.List;

/**
 * Repository interface for managing {@code DailyReportData} entities.
 * Extends {@code JpaRepository} to provide CRUD operations and additional
 * custom-defined database query methods for {@code DailyReportData}.
 * <p>
 * Methods:
 * - {@code findAllSensorReadingsByOwnerUsername}: Retrieves a list of {@code DailyReportData}
 *   associated with a specific user's username.
 * - {@code findByFloraLink_Id}: Fetches a single {@code DailyReportData} entity by the unique identifier
 *   of the associated flora link.
 * <p>
 * Annotations:
 * - {@code @Query}: Defines custom database queries using JPQL to fetch data based on specified conditions.
 */
public interface DailyReportDataRepository extends JpaRepository<DailyReportData, Integer> {
    @Query("SELECT s " +
            "FROM DailyReportData s " +
            "WHERE s.floraeUser.username = :username")
    List<DailyReportData> findAllSensorReadingsByOwnerUsername(String username);
    DailyReportData findByFloraLink_Id(Integer floraLinkId);
}
