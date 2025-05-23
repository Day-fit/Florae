package pl.Dayfit.Florae.Repositories.JPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.PlantRequirements;

/**
 * Repository interface for managing {@code PlantRequirements} entities.
 * Extends {@code JpaRepository} to provide CRUD operations and additional
 * custom-defined database query methods for {@code PlantRequirements}.

 * Methods:
 * - {@code getPlantRequirementsByPid}: Retrieves the plant requirements based on a specific plant identifier (PID).

 * Annotations:
 * - {@code @Repository}: Marks this interface as a Spring Data repository.
 */
@Repository
public interface PlantRequirementsRepository extends JpaRepository<PlantRequirements, Integer> {
    PlantRequirements getPlantRequirementsByPid(String pid);
}
