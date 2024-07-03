package it.uniroma3.siw.model;

import java.util.List; 
import java.util.Map;
import java.util.Objects;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;


@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String name;
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RecipeImage> recipeImages;
    
    @Lob
    private String description;
    
    @ManyToOne
    private Chef inventor;
    
    @ElementCollection
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @MapKeyJoinColumn(name = "ingredient_id")
    @Column(name = "quantity")
    private Map<Ingredient, Long> ingredients;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


	public List<RecipeImage> getRecipeImages() {
		return recipeImages;
	}

	public void setRecipeImages(List<RecipeImage> recipeImages) {
		this.recipeImages = recipeImages;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Chef getInventor() {
        return inventor;
    }

    public void setInventor(Chef inventor) {
        this.inventor = inventor;
    }

    public Map<Ingredient, Long> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<Ingredient, Long> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, inventor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Recipe other = (Recipe) obj;
        return Objects.equals(name, other.name) && inventor.equals(other.inventor);
    }
}
