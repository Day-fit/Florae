package pl.Dayfit.Florae.Repositories.Redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import pl.Dayfit.Florae.Entities.FloraLink.CurrentData;

@Repository
public interface CurrentReadingsRepository extends CrudRepository<CurrentData, String> {
    CurrentData findByDeviceId(String deviceId);
}
