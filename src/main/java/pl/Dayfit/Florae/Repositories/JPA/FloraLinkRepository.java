package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.FloraLink;
import pl.Dayfit.Florae.Entities.FloraeUser;

import java.util.List;

@Repository
public interface FloraLinkRepository extends JpaRepository<FloraLink, Integer> {
    List<FloraLink> findByOwner(FloraeUser owner);
    @Query("SELECT f.linkedFloraLink FROM ApiKey f WHERE f.keyValue = :apiKey")
    FloraLink findByApiKey(String apiKey);
}
