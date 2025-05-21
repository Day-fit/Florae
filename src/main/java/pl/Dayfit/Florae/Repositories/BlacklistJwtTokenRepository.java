package pl.Dayfit.Florae.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.BlacklistJwtToken;

@Repository
public interface BlacklistJwtTokenRepository extends JpaRepository<BlacklistJwtToken, Integer> {
    Boolean existsByToken(String refreshToken);
}
