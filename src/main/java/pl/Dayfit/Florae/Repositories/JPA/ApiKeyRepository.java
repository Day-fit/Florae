package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.ApiKey;

import java.util.List;

/**
 * Repository interface for managing {@code ApiKey} entities.
 * Extends {@code JpaRepository} to provide CRUD operations and additional
 * custom-defined database query methods for {@code ApiKey}.

 * Methods:
 * - {@code findAllByKeyValue}: Retrieves an {@code ApiKey} based on its key value.
 * - {@code findUnusedApiKeysBeforeDate}: Retrieves a list of unused API keys created before a specified date.
 * - {@code deleteRevokedApiKeys}: Deletes API keys that have been marked as revoked.

 * Annotations:
 * - {@code @Repository}: Marks this interface as a Spring Data repository.
 * - {@code @Query}: Provides custom queries for retrieving and modifying API key data.
 * - {@code @Modifying}: Indicates that the associated query performs an update or delete operation.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Integer> {
    ApiKey findByKeyValue(String keyValue);
    List<ApiKey> findByShortKey(String shortKey);

    @Query("SELECT a FROM ApiKey a" +
            " WHERE a.id = :id" +
            " AND a.isRevoked = FALSE" +
            " AND a.linkedFloraLink IS NULL")
    List<ApiKey> findUnusedApiKey(Integer id);

    @Modifying
    @Query("DELETE FROM ApiKey a WHERE a.isRevoked = TRUE")
    int deleteRevokedApiKeys();
    List<ApiKey> findAllByShortKey(String shortKey);
}
