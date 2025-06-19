package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.FloraeUser;

/**
 * Repository interface for managing {@code FloraeUsers} entities.
 * Extends {@code JpaRepository} to provide CRUD operations and additional
 * custom-defined database query methods for {@code FloraeUsers}.

 * Methods:
 * - {@code findByUsername}: Retrieves a {@code FloraeUsers} by their username.
 * - {@code existsByEmailOrUsername}: Checks if a user exists with the given email or username.

 * Annotations:
 * - {@code @Repository}: Marks this interface as a Spring Data repository.
 */
@Repository
public interface FloraeUserRepository extends JpaRepository<FloraeUser, Integer> {
    FloraeUser findByUsername(String username);
    FloraeUser findByEmail(String email);
    FloraeUser findByUsernameOrEmail(String username, String email);
}
