package pl.Dayfit.Florae.Repositories;

import org.springframework.data.repository.CrudRepository;

import pl.Dayfit.Florae.Entities.Plant;

public interface PhotosRepository extends CrudRepository<Plant, Integer>{

    
}