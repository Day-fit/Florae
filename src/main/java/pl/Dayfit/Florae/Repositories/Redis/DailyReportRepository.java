package pl.Dayfit.Florae.Repositories.Redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.Redis.DailyReport;

@Repository
public interface DailyReportRepository extends CrudRepository<DailyReport, String> {
    DailyReport findDailyReportById(String id);
}
