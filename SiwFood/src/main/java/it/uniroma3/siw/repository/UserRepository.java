package it.uniroma3.siw.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
    
    // Metodo personalizzato per trovare un utente per nome e cognome
    Optional<User> findByNameAndSurname(String name, String surname);
}