package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.BlacklistJwtToken;

import java.util.Date;

@Repository
public interface BlacklistJwtTokenRepository extends JpaRepository<BlacklistJwtToken, Integer> {
    Boolean existsByToken(String refreshToken);

    @Modifying
    @Query("DELETE FROM BlacklistJwtToken e WHERE e.expiryDate > :now")
    int deleteAllExpiredTokens(Date now); //If the token is expired, it can no longer be used, so we can remove it from the database.
}
