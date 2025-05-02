package pl.Dayfit.Florae.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import pl.Dayfit.Florae.Entities.Plant;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Integer> {
}