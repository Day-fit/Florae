package pl.Dayfit.Florae.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.PlantRequirements;

@Repository
public interface PlantRequirementsRepository extends JpaRepository<PlantRequirements, Integer> {
    PlantRequirements getPlantRequirementsByPid(String pid);
}
