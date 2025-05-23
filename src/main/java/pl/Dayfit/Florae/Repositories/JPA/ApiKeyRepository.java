package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.ApiKey;

import java.time.Instant;
import java.util.List;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Integer> {
    ApiKey findAllByKeyValue(String keyValue);

    @Query("SELECT a FROM ApiKey a" +
            " WHERE a.createdDate <= :date" +
            " AND a.isRevoked = FALSE" +
            " AND a.linkedFloraLink IS NULL")
    List<ApiKey> findUnusedApiKeysBeforeDate(Instant date);

    @Modifying
    @Query("DELETE FROM ApiKey a WHERE a.isRevoked = TRUE")
    int deleteRevokedApiKeys();
}
