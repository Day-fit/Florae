package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.FloraLink;

@Repository
public interface FloraLinkRepository extends JpaRepository<FloraLink, Integer> {
}
