package tn.esprit.brainwaveusermanagement.Services;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Exceptions.PendingAccountException;
import tn.esprit.brainwaveusermanagement.Repositories.PersonRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private PersonRepository personRepository;

    @Override
        public UserDetails loadUserByUsername(String email) {
            // Retrieve the user from the repository
            Person person = personRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the user status is 'PENDING'
        if ("PENDING".equals(person.getStatus())) {
            throw new PendingAccountException("Account is pending approval.") {};
        }

            // Initialize authorities as an empty list
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // Add the role to the authorities list
            if (person.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + person.getRole().name()));
            }

            // Return an instance of User with the person's email, password, and roles
            return new User(person.getEmail(), person.getPassword(), authorities);
        }
}
