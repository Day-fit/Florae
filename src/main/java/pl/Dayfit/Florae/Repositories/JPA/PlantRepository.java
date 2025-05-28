package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.Plant;

import java.util.List;

/**
 * Repository interface for managing {@code Plant} entities.
 * Extends {@code JpaRepository} to provide CRUD operations and additional
 * custom-defined database query methods for {@code Plant}.

 * Methods:
 * - {@code getPlantsByUsername}: Retrieves a list of plants associated with a specific username.

 * Annotations:
 * - {@code @Repository}: Marks this interface as a Spring Data repository.
 * - {@code @Query}: Provides a custom query to retrieve plants associated with a user's username.
 */
@Repository
public interface PlantRepository extends JpaRepository<Plant, Integer> {
    @Query("SELECT p FROM Plant p " +
            "WHERE p.linkedUser.username = :username")
    List<Plant> getPlantsByUsername(@Param("username") String username);
}