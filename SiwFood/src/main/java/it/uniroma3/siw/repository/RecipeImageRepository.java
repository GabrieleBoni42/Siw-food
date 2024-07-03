package it.uniroma3.siw.repository;
import org.springframework.data.jpa.repository.JpaRepository; 
import it.uniroma3.siw.model.RecipeImage;
import java.util.List;

public interface RecipeImageRepository extends JpaRepository<RecipeImage, Long> {
    List<RecipeImage> findByRecipeId(Long recipeId);
}
