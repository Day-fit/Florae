package pl.Dayfit.Florae.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.PlantRequirements;

@Repository
public interface PlantRequirementsRepository extends JpaRepository<PlantRequirements, Integer> {
    @Query("SELECT r FROM PlantRequirements r " +
            "WHERE r.pid = :pid")
    PlantRequirements getPlantRequirementsByPid(@Param("pid") String pid);
}
