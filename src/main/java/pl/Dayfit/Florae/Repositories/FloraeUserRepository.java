package pl.Dayfit.Florae.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.FloraeUser;

@Repository
public interface FloraeUserRepository extends JpaRepository<FloraeUser, Integer> {
    FloraeUser findByUsername(String username);
    boolean existsByEmailOrUsername(String email, String username);
}
