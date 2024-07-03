package it.uniroma3.siw.controller;

import java.io.IOException; 
import java.util.ArrayList;    
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.controller.validator.IngredientValidator;
import it.uniroma3.siw.controller.validator.RecipeValidator;
import it.uniroma3.siw.model.RecipeImage;
import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ChefRepository;
import it.uniroma3.siw.repository.IngredientRepository;
import it.uniroma3.siw.repository.RecipeImageRepository;
import it.uniroma3.siw.repository.RecipeRepository;
import it.uniroma3.siw.service.CredentialsService;
 
@Controller
@org.springframework.transaction.annotation.Transactional
public class RecipeController {
	@Autowired 
	private RecipeRepository recipeRepository;
	
	@Autowired 
	private RecipeImageRepository recipeImageRepository;
	
	@Autowired 
	private ChefRepository chefRepository;
	
	@Autowired 
	private IngredientRepository ingredientRepository;

	@Autowired 
	private RecipeValidator recipeValidator;
	
	@Autowired 
	private IngredientValidator ingredientValidator;
	
	@Autowired 
	private CredentialsService credentialsService;
	



	@GetMapping("/public/recipe/{id}")
	public String getRecipe(@PathVariable("id") Long id, Model model) {
	    Recipe recipe = this.recipeRepository.findById(id).orElse(null);
	    if (recipe != null) {
				List<String> base64Strings = recipe.getRecipeImages().stream()
						.filter(Objects::nonNull)
						.map(recipeImage -> Base64Utils.encodeToString(recipeImage.getImageData()))
						.collect(Collectors.toList());
	        model.addAttribute("recipe", recipe);
	        model.addAttribute("base64Strings", base64Strings);
	        return "public/recipe";
	    } else {
	        return "recipeNotFound";
	    }
	}


	@GetMapping("/public/recipes")
	public String getRecipes(Model model) {
	    List<Recipe> recipes = (List<Recipe>) this.recipeRepository.findAll();
	    Map<Long, String> recipeFirstImages = new HashMap<>();

	    for (Recipe recipe : recipes) {
	        if (!recipe.getRecipeImages().isEmpty()) {
	            RecipeImage firstImage = recipe.getRecipeImages().get(0);
	            String base64Image = Base64Utils.encodeToString(firstImage.getImageData());
	            recipeFirstImages.put(recipe.getId(), base64Image);
	        }
	    }

	    model.addAttribute("recipes", recipes);
	    model.addAttribute("recipeFirstImages", recipeFirstImages);
	    return "public/recipes.html";
	}
	
	@GetMapping("public/formSearchRecipes")
	public String formSearchMovies() {
		return "public/formSearchRecipes.html";
	}

	@PostMapping("public/searchRecipes")
	public String searchMovies(Model model, @RequestParam String name) {
		model.addAttribute("recipes", this.recipeRepository.findByName(name));
		return "public/foundRecipes.html";
	}
	
	  @PostMapping("admin/deleteRecipeAdmin/{id}")
	    public String deleteRecipeAdmin(@PathVariable Long id) {
	        recipeRepository.deleteById(id);
	        return "redirect:/admin/manageRecipesAdmin";
	    }
	  
	  @PostMapping("/chef/deleteRecipeChef/{id}")
	    public String deleteRecipeChef(@PathVariable Long id, RedirectAttributes redirectAttributes) {
	        // Ottieni l'utente autenticato
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
	        User user = credentials.getUser();

	        // Trova lo chef associato all'utente autenticato
	        Chef chef = chefRepository.findByNameAndSurname(user.getName(), user.getSurname());

	        // Trova la ricetta da eliminare
	        Recipe recipe = recipeRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id:" + id));

	        // Controlla se lo chef è l'inventore della ricetta
	        if (!recipe.getInventor().equals(chef)) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Non sei autorizzato a cancellare questa ricetta.");
	            return "redirect:/chef/manageRecipesChef";
	        }

	        recipeRepository.delete(recipe);
	        return "redirect:/chef/manageRecipesChef";
	    }
	  
	    
		@GetMapping("/admin/manageRecipesAdmin")
		public String manageRecipesAdmin(Model model) {
			model.addAttribute("recipes", this.recipeRepository.findAll());
			return "admin/manageRecipesAdmin.html";
		}
		
		 @GetMapping("/chef/manageRecipesChef")
		    public String manageRecipesChef(Model model) {
		        // Ottieni l'utente autenticato
		        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		        User user = credentials.getUser();

		        // Trova lo chef associato all'utente autenticato
		        Chef chef = chefRepository.findByNameAndSurname(user.getName(), user.getSurname());

		        model.addAttribute("authenticatedChef", chef);
		        model.addAttribute("recipes", recipeRepository.findAll());
		        return "chef/manageRecipesChef";
		    }
		 
	    @GetMapping("/chef/formNewRecipe")
	    public String formNewRecipe(Model model) {
	        // Ottieni l'utente autenticato
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
	        User user = credentials.getUser();

	        // Trova lo chef associato all'utente autenticato
	        Chef chef = this.chefRepository.findByNameAndSurname(user.getName(), user.getSurname());

	        model.addAttribute("chef", chef);
	        model.addAttribute("recipe", new Recipe());
	        model.addAttribute("ingredients", ingredientRepository.findAll());
	        return "/chef/formNewRecipe.html";
	    }

	    @PostMapping("/chef/newRecipe")
	    public String createNewRecipe(@Valid @ModelAttribute Recipe recipe,
                @RequestParam Map<String, String> ingredientQuantities,
                @RequestParam("uploadedImages") MultipartFile[] uploadedImages,
                BindingResult result,
                Model model,
                @RequestParam("chefId") Long chefId) {
if (result.hasErrors()) {
model.addAttribute("ingredients", ingredientRepository.findAll());
return "formNewRecipe";
}

	        // Trova lo chef dal repository utilizzando l'ID passato come parametro
	        Chef chef = chefRepository.findById(chefId)
	                .orElseThrow(() -> new IllegalArgumentException("Chef non trovato con ID: " + chefId));

	        // Imposta lo chef come inventore della ricetta
	        recipe.setInventor(chef);

	        // Mappa per salvare gli ingredienti e le relative quantità
	        Map<Ingredient, Long> ingredientsMap = new HashMap<>();

	        // Itera sui parametri del form per ottenere gli ingredienti e le quantità
	        for (Map.Entry<String, String> entry : ingredientQuantities.entrySet()) {
	            String paramName = entry.getKey();
	            if (paramName.startsWith("ingredientQuantities")) {
	                Long ingredientId = Long.parseLong(paramName.substring(paramName.indexOf("[") + 1, paramName.indexOf("]")));
	                Ingredient ingredient = ingredientRepository.findById(ingredientId)
	                        .orElseThrow(() -> new IllegalArgumentException("Ingrediente non trovato con ID: " + ingredientId));
	                // Controlla se il valore della quantità è vuoto e imposta a 0 se lo è
	                Long quantity = entry.getValue().isEmpty() ? 0L : Long.parseLong(entry.getValue());
	                ingredientsMap.put(ingredient, quantity);
	            }
	        }

	        // Imposta la mappa degli ingredienti sulla ricetta e salva
	        recipe.setIngredients(ingredientsMap);
	        
	        try {
	            // Salva le immagini
	            List<RecipeImage> recipeImages = new ArrayList<>();

	            for (MultipartFile uploadedImage : uploadedImages) {
	                RecipeImage recipeImage = new RecipeImage();
	                byte[] imageBytes = uploadedImage.getBytes();
	                recipeImage.setImageData(imageBytes);
	                recipeImage.setRecipe(recipe);  // Imposta l'istanza di Recipe
	                recipeImages.add(recipeImage);
	            }

	            // Salva l'istanza di Recipe
	            recipeRepository.save(recipe);

	            // Salva le immagini della ricetta
	            recipeImageRepository.saveAll(recipeImages);

	            // Aggiorna la lista delle immagini nella ricetta
	            recipe.setRecipeImages(recipeImages);
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "redirect:/error";
	        }

	        return "redirect:/public/recipes";
	    }




	    @GetMapping("/public/filterRecipes")
	    public String filteredRecipesByName(@RequestParam(name = "name", required = false) String name, Model model) {
	        List<Recipe> filteredRecipes;

	        // Utilizziamo il metodo findByName del repository per cercare le ricette per nome
	        if (name != null && !name.isEmpty()) {
	            filteredRecipes = recipeRepository.findByName(name);
	        } else {
	            // Se il parametro nome è vuoto o non specificato, restituisci tutte le ricette
	            filteredRecipes = (List<Recipe>) recipeRepository.findAll();
	        }
	        
	        Map<Long, String> recipeFirstImages = new HashMap<>();

		    for (Recipe recipe : filteredRecipes) {
		        if (!recipe.getRecipeImages().isEmpty()) {
		            RecipeImage firstImage = recipe.getRecipeImages().get(0);
		            String base64Image = Base64Utils.encodeToString(firstImage.getImageData());
		            recipeFirstImages.put(recipe.getId(), base64Image);
		        }
		    }

		    model.addAttribute("recipes", filteredRecipes);
		    model.addAttribute("recipeFirstImages", recipeFirstImages);
	        return "public/recipes"; // Assumiamo che ci sia una pagina chiamata recipes.html per visualizzare la lista delle ricette
	    }


//	
//	@GetMapping("/admin/updateActors/{id}")
//	public String updateActors(@PathVariable("id") Long id, Model model) {
//
//		List<Chef> actorsToAdd = this.actorsToAdd(id);
//		model.addAttribute("actorsToAdd", actorsToAdd);
//		model.addAttribute("movie", this.movieRepository.findById(id).get());
//
//		return "admin/actorsToAdd.html";
//	}
//
//	@GetMapping(value="/admin/addActorToMovie/{actorId}/{movieId}")
//	public String addActorToMovie(@PathVariable("actorId") Long actorId, @PathVariable("movieId") Long movieId, Model model) {
//		Recipe recipe = this.movieRepository.findById(movieId).get();
//		Chef actor = this.artistRepository.findById(actorId).get();
//		Set<Chef> actors = recipe.getActors();
//		actors.add(actor);
//		this.movieRepository.save(recipe);
//		
//		List<Chef> actorsToAdd = actorsToAdd(movieId);
//		
//		model.addAttribute("movie", recipe);
//		model.addAttribute("actorsToAdd", actorsToAdd);
//
//		return "admin/actorsToAdd.html";
//	}
//	
//	@GetMapping(value="/admin/removeActorFromMovie/{actorId}/{movieId}")
//	public String removeActorFromMovie(@PathVariable("actorId") Long actorId, @PathVariable("movieId") Long movieId, Model model) {
//		Recipe recipe = this.movieRepository.findById(movieId).get();
//		Chef actor = this.artistRepository.findById(actorId).get();
//		Set<Chef> actors = recipe.getActors();
//		actors.remove(actor);
//		this.movieRepository.save(recipe);
//
//		List<Chef> actorsToAdd = actorsToAdd(movieId);
//		
//		model.addAttribute("movie", recipe);
//		model.addAttribute("actorsToAdd", actorsToAdd);
//
//		return "admin/actorsToAdd.html";
//	}
//
//	private List<Chef> actorsToAdd(Long movieId) {
//		List<Chef> actorsToAdd = new ArrayList<>();
//
//		for (Chef a : artistRepository.findActorsNotInMovie(movieId)) {
//			actorsToAdd.add(a);
//		}
//		return actorsToAdd;
//	}
}
