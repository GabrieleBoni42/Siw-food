package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.Recipe;

public interface RecipeRepository extends CrudRepository<Recipe, Long> {

	@Query("SELECT r FROM Recipe r WHERE lower(r.name) like lower(concat('%', :name, '%'))")
	public List<Recipe> findByName(String name);

	public boolean existsByNameAndInventor(String name, Chef chef);	

}