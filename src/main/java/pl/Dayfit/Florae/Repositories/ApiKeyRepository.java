package pl.Dayfit.Florae.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.ApiKey;

import java.time.Instant;
import java.util.List;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Integer> {
    ApiKey getApiKeysByValue(String value);

    @Query("SELECT a FROM ApiKey a" +
            " WHERE a.createdDate <= :date" +
            " AND a.isRevoked = FALSE" +
            " AND a.linkedFloraLink = NULL")
    List<ApiKey> findUnusedApiKeysBeforeDate(Instant date);
}
