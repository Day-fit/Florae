package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;

import java.util.List;

/**
 * Repository interface for managing {@code FloraLink} entities.
 * Extends {@code JpaRepository} to provide CRUD operations and
 * additional custom-defined database query methods for {@code FloraLink}.

 * Methods:
 * - {@code findByOwner}: Retrieves a list of {@code FloraLink} entities
 *   associated with a specific {@code FloraeUser}.
 * - {@code findByApiKey}: Retrieves a {@code FloraLink} entity associated
 *   with a specific API key.

 * Annotations:
 * - {@code @Repository}: Marks this interface as a Spring Data repository.
 * - {@code @Query}: Provides a custom query to retrieve the {@code FloraLink}
 *   entity associated with an API key.
 */
@Repository
public interface FloraLinkRepository extends JpaRepository<FloraLink, Integer> {
    List<FloraLink> findByOwner(FloraeUser owner);
    @Query("SELECT f.linkedFloraLink FROM ApiKey f WHERE f.keyValue = :apiKey")
    FloraLink findByApiKey(String apiKey);
}
