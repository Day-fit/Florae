package pl.Dayfit.Florae.Repositories.Redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.Sensors.CurrentReportData;

import java.util.List;

@Repository
@EnableRedisRepositories
public interface CurrentReportDataRepository extends CrudRepository<CurrentReportData, String> {
    CurrentReportData findByFloraLinkId(String floraLinkId);
    List<CurrentReportData> findAllByOwnerUsername(String username);
}