package tn.esprit.brainwaveusermanagement.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Entities.UserStatus;
import tn.esprit.brainwaveusermanagement.Repositories.PersonRepository;
import tn.esprit.brainwaveusermanagement.dto.LoginRequest;
import tn.esprit.brainwaveusermanagement.Utils.JwtUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/authentification")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public LoginController(AuthenticationManager authenticationManager, PersonRepository personRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Log the incoming request for debugging
        System.out.println("Login attempt for email: " + request.getEmail());

        // Retrieve the user by email
        Person person = personRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Log if user is found
        System.out.println("User found: " + person.getEmail());

        // Check if the user's status is PENDING
        if (person.getStatus() == UserStatus.PENDING) {
            // If the account is pending approval, return an error message
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account is pending approval by the admin.");
        }

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), person.getPassword())) {
            System.out.println("Invalid credentials for user: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // Log successful password validation
        System.out.println("Password valid for user: " + request.getEmail());

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Set authentication context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String token = jwtUtils.generateJwtToken(authentication);
        System.out.println("Generated JWT token: " + token);

        // Return token in response
        Map<String, String> response = new HashMap<>();
        response.put("token", "Bearer " + token);
        response.put("email", person.getEmail());

        return ResponseEntity.ok(response);
    }



}
