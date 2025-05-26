package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.BlacklistJwtToken;

import java.util.Date;

/**
 * Repository interface for managing {@code BlacklistJwtToken} entities.
 * Extends {@code JpaRepository} to provide CRUD operations and additional
 * custom-defined database query methods for {@code BlacklistJwtToken}.

 * Methods:
 * - {@code existsByToken}: Checks if a specific token exists in the blacklist.
 * - {@code deleteAllExpiredTokens}: Deletes all expired tokens from the blacklist.
 *   A token is considered expired if its expiry date is past the specified time.

 * Annotations:
 * - {@code @Repository}: Marks this interface as a Spring Data repository.
 * - {@code @Modifying}: Indicates a modifying query for the database.
 * - {@code @Query}: Provides a custom query to delete all expired tokens.
 */
@Repository
public interface BlacklistJwtTokenRepository extends JpaRepository<BlacklistJwtToken, Integer> {
    Boolean existsByToken(String refreshToken);

    @Modifying
    @Query("DELETE FROM BlacklistJwtToken e WHERE e.expiryDate > :now")
    int deleteAllExpiredTokens(Date now); //If the token is expired, it can no longer be used, so we can remove it from the database.
}
