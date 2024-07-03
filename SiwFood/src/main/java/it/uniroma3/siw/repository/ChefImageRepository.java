package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.uniroma3.siw.model.ChefImage;

@Repository
public interface ChefImageRepository extends JpaRepository<ChefImage, Long> {
	 ChefImage findByChefId(Long chefId);
}

