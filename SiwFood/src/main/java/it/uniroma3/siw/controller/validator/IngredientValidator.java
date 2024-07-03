package it.uniroma3.siw.controller.validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.repository.IngredientRepository;

@Component
public class IngredientValidator implements Validator {
    @Autowired
    private IngredientRepository ingredientRepository;

    @Override
    public void validate(Object o, Errors errors) {
        Ingredient ingredient = (Ingredient) o;
        if (ingredient.getName() != null && ingredientRepository.existsByName(ingredient.getName())) {
            errors.rejectValue("name", "ingredient.duplicate", "L'ingrediente con questo nome gia' esiste!");
        }
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Ingredient.class.equals(aClass);
    }
}
