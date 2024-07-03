package it.uniroma3.siw.controller;

import java.io.IOException; 
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

import it.uniroma3.siw.model.Chef;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.ChefImage;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.RecipeImage;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ChefRepository;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.ChefImageRepository;
import it.uniroma3.siw.repository.UserRepository;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.repository.ChefRepository;

@Controller
@Transactional
public class ChefController {
	
	@Autowired 
	private ChefRepository chefRepository;
	@Autowired
	 private CredentialsRepository credentialsRepository;
	@Autowired
	 private UserRepository userRepository;
	@Autowired
	private CredentialsService credentialsService;
	@Autowired
	private ChefImageRepository chefImageRepository;


	
	@GetMapping("/chef/updateAccount")
	public String showUpdateProfileForm(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
            User user = credentials.getUser();
             
            Chef chef = this.chefRepository.findByNameAndSurname(user.getName(), user.getSurname());

	        model.addAttribute("chef", chef); // Passa solo l'oggetto Chef al modello
	        return "chef/formUpdateAccount.html"; // Ritorna il template per l'aggiornamento
	    } else {
	        return "redirect:/public/chefs"; // Se non trova l'utente, reindirizza
	    }
	}


	@PostMapping("/chef/updateAcc")
	public String updateChef(
	    @Valid @ModelAttribute("chef") Chef chef,
	    BindingResult bindingResult,
	    @RequestPart(value = "uploadedImage", required = false) MultipartFile uploadedImage,
	    Model model,
	    @RequestParam("id") Long chefId // Aggiunta del parametro per l'ID dello chef
	) {
	    if (bindingResult.hasErrors()) {
	        return "chef/formUpdateAccount.html"; // Rimani nel form in caso di errori
	    }

	    try {
	        // Trova lo chef nel repository utilizzando l'ID passato dal modulo HTML
	        Chef existingChef = chefRepository.findById(chefId)
	            .orElseThrow(() -> new IllegalArgumentException("Lo chef con l'ID fornito non esiste."));

	        // Aggiorna i campi dello chef
	        existingChef.setDateOfBirth(chef.getDateOfBirth());

	        // Verifica se è stata fornita un'immagine e aggiorna l'immagine solo se c'è un file
	        if (uploadedImage != null && !uploadedImage.isEmpty()) {
	            byte[] imageData = uploadedImage.getBytes();
	            ChefImage chefImage = new ChefImage(imageData);
	            chefImageRepository.save(chefImage); // Salva l'istanza ChefImage nel repository

	            existingChef.setImage(chefImage); // Associa l'immagine allo chef
	        }
	        // Salva le modifiche nello chef esistente nel repository
	        chefRepository.save(existingChef);

	        return "redirect:/public/chefs"; // Reindirizza alla lista degli chef dopo l'aggiornamento

	    } catch (IOException e) {
	        // Gestione dell'errore di IO
	        return "chef/formUpdateAccount.html"; // Torna alla pagina di aggiornamento in caso di errore
	    }
	}
	
	@GetMapping("/admin/manageChefs")
	public String manageChefs(Model model) {
		model.addAttribute("chefs", this.chefRepository.findAll());
		return "admin/manageChefs.html";
	}
//
	@GetMapping("/public/chefs")
	public String getChefs(Model model) {
	    List<Chef> chefs = (List<Chef>) chefRepository.findAll();

	    for (Chef chef : chefs) {
	        ChefImage chefImage = chef.getImage();
	        if (chefImage != null && chefImage.getImageData() != null) {
	            String base64Image = chefImage.getImageBase64(); // Ottenere Base64 direttamente dall'oggetto ChefImage
	            model.addAttribute("base64Image_" + chef.getId(), base64Image);
	        } else {
	            System.out.println("Nessuna immagine trovata per l'chef con ID: " + chef.getId());
	        }
	    }
	    

	    model.addAttribute("chefs", chefs);
	    return "public/chefs";
	}



	
	@PostMapping("admin/deleteChef/{id}")
	public String deleteChef(@PathVariable Long id) {
	    // Trova lo chef da eliminare
	    Chef chef = chefRepository.findById(id).orElse(null);

	    if (chef != null) {
	        // Trova l'utente con lo stesso nome e cognome dello chef
	        User user = userRepository.findByNameAndSurname(chef.getName(), chef.getSurname()).orElse(null);

	        if (user != null) {
	            // Trova le credenziali associate all'utente
	            Credentials credentials = credentialsRepository.findByUser(user).orElse(null);

	            // Elimina le credenziali, se esistono
	            if (credentials != null) {
	                credentialsRepository.delete(credentials);
	            }
	           
	        }

	        // Elimina lo chef
	        chefRepository.delete(chef);
	        
	    } else {
	        // Se lo chef non è trovato, reindirizza con un messaggio di errore
	        return "redirect:/admin/manageChefs?error=chefNotFound";
	    }

	    return "redirect:/admin/manageChefs";
	}

    
    @GetMapping("/public/chef/{id}")
    public String getChefAndRecipes(@PathVariable("id") Long id, Model model) {
        Chef chef = chefRepository.findById(id).orElse(null);

        if (chef != null) {
            // Aggiungi lo chef al modello
            model.addAttribute("chef", chef);

            // Recupera le ricette dello chef
            List<Recipe> recipes = chef.getInventorOf();
            
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
            
            
            
            

            // Recupera l'immagine dello chef e convertila in Base64
            ChefImage chefImage = chef.getImage();
            if (chefImage != null) {
                String base64Image = Base64.getEncoder().encodeToString(chefImage.getImageData());
                model.addAttribute("base64Image", base64Image); // Aggiungi al modello
            } else {
                System.out.println("L'immagine dello chef non è stata trovata"); // Debug
            }
        } else {
            System.out.println("Chef non trovato con ID: " + id); // Debug
        }

        return "public/chef"; // Nome del template HTML
    }

    @GetMapping("/chef/indexChef")
    public String chefIndex(Model model) {
        // Ottieni l'autenticazione corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
                User user = credentials.getUser();
                    model.addAttribute("nome", user.getName());
                    model.addAttribute("cognome", user.getSurname());
                
            
        }

        return "chef/indexChef.html"; // Ritorna la pagina dell'index degli chef
    }


	
    @GetMapping("/public/filterChefs")
    public String filteredChefsBySurname(@RequestParam(name = "surname", required = false) String surname, Model model) {
        List<Chef> filteredChefs;

        // Utilizziamo il metodo findBySurname del repository per cercare gli chef per cognome
        if (surname != null && !surname.isEmpty()) {
            filteredChefs = chefRepository.findBySurname(surname);
        } else {
            // Se il parametro cognome è vuoto o non specificato, restituisci tutti gli chef
            filteredChefs = (List<Chef>) chefRepository.findAll();
        }

        for (Chef chef : filteredChefs) {
            ChefImage chefImage = chef.getImage();
            if (chefImage != null) {
                String base64Image = Base64.getEncoder().encodeToString(chefImage.getImageData());
                model.addAttribute("base64Image_" + chef.getId(), base64Image); // Aggiungi al modello con un nome unico per ogni chef
            } else {
                System.out.println("L'immagine dello chef non è stata trovata per chef ID: " + chef.getId()); // Debug
            }
        }

        model.addAttribute("chefs", filteredChefs);
        return "public/chefs"; // Assumi che ci sia una pagina chiamata chef-list.html per visualizzare la lista degli chef
    }

}
