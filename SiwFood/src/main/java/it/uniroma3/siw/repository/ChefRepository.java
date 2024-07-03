package it.uniroma3.siw.repository;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Chef;

public interface ChefRepository extends CrudRepository<Chef, Long> {

	public boolean existsByNameAndSurname(String name, String surname);	

	@Query("SELECT c FROM Chef c " +
            "WHERE (:surname IS NULL OR UPPER(c.surname) = UPPER(:surname))")
	public List<Chef> findBySurname(@Param("surname") String surname);
	
    @Query("SELECT c FROM Chef c " +
            "WHERE (:name IS NULL OR UPPER(c.name) = UPPER(:name)) " +
            "AND (:surname IS NULL OR UPPER(c.surname) = UPPER(:surname))")
     Chef findByNameAndSurname(@Param("name") String name, @Param("surname") String surname);


}