package tn.esprit.brainwaveusermanagement.Services;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Repositories.PersonRepository;
import tn.esprit.brainwaveusermanagement.Utils.JwtUtils;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service

public class PersonService {

    private PersonRepository personRepository;
    private final JwtUtils jwtUtils;

    public PersonService(PersonRepository personRepository, JwtUtils jwtUtils) {
        this.personRepository = personRepository;
        this.jwtUtils = jwtUtils;
    }
    public Person findUserByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));

    }

    public Person getPersonByCin(String cin) {
        return personRepository.findByCin(cin);  // Fetch person by CIN
    }

    public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

    public boolean existsByCin(String cin) {
        return personRepository.existsByCin(cin);}

    public void savePerson(Person person) {
        personRepository.save(person);  // Save updated person entity
    }

    public Person getAuthenticatedUser(String token) {
        // Extract email from the token
        String email = jwtUtils.extractUsername(token);

        // Fetch user from database
        return personRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

