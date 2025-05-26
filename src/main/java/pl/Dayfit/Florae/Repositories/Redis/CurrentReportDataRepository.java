package pl.Dayfit.Florae.Repositories.Redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.Sensors.CurrentReportData;

import java.util.List;

/**
 * Repository interface for managing CurrentReportData entities stored in Redis.

 * This repository provides methods for performing basic CRUD operations and
 * includes additional query methods to fetch data based on domain-specific fields.

 * Annotations:
 * - {@code @Repository}: Indicates that this interface is a repository used for data operations.
 * - {@code @EnableRedisRepositories}: Configures Redis repositories to be enabled for this interface.

 * Extends:
 * - {@code CrudRepository<CurrentReportData, String>}: Provides basic CRUD functionality
 *   for CurrentReportData entities with String as the ID type.

 * Methods:
 * - {@code findByFloraLinkId(String floraLinkId)}: Retrieves a CurrentReportData entity by its associated floraLinkId.
 * - {@code findAllByOwnerUsername(String username)}: Retrieves a list of CurrentReportData entities
 *   associated with a specific owner username.
 */
@Repository
@EnableRedisRepositories
public interface CurrentReportDataRepository extends CrudRepository<CurrentReportData, String> {
    CurrentReportData findByFloraLinkId(String floraLinkId);
    List<CurrentReportData> findAllByOwnerUsername(String username);
}