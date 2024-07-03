package it.uniroma3.siw.model;

import java.util.Base64;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class RecipeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private byte[] imageData; // Sequenza di byte dell'immagine
    
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    // Costruttore, getter e setter
    // Metodi aggiuntivi

    public RecipeImage() {
    }

    public RecipeImage(byte[] imageData) {
        this.imageData = imageData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

    // Metodo per ottenere la rappresentazione Base64 dell'immagine
    public String getImageBase64() {
        return Base64.getEncoder().encodeToString(imageData);
    }

    // Metodo per convertire una stringa Base64 in byte array
    public static byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
