package tn.esprit.brainwaveusermanagement.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import tn.esprit.brainwaveusermanagement.Services.PasswordResetService;
import tn.esprit.brainwaveusermanagement.Services.ReCaptchaService;
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
    private final ReCaptchaService reCaptchaService;
    private PasswordResetService passwordResetService;

    public LoginController(AuthenticationManager authenticationManager, PersonRepository personRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,ReCaptchaService reCaptchaService,PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.reCaptchaService=reCaptchaService;
        this.passwordResetService=passwordResetService;
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


        // Check if the user's account is banned
        if (person.isBanned()) {
            // If the account is banned, return a FORBIDDEN status with a message
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account has been banned. Please contact support.");
        }

        // Check if the user's status is PENDING
        if (person.getStatus() == UserStatus.PENDING) {
            // If the account is pending approval, return an error message
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account is pending approval by the admin.");
        }

        if (request.getRecaptchaToken() == null || request.getRecaptchaToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("reCAPTCHA token is required.");
        }

        if (!reCaptchaService.verifyRecaptcha(request.getRecaptchaToken())) {
            return ResponseEntity.badRequest().body("Invalid reCAPTCHA.");
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

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        passwordResetService.sendPasswordResetEmail(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset email sent successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-token/{token}")
    public ResponseEntity<?> verifyToken(@PathVariable String token) {
        String email = passwordResetService.validateToken(token);
        if (email != null) {
            return ResponseEntity.ok().build(); // Token is valid
        } else {
            return ResponseEntity.badRequest().build(); // Token is invalid
        }
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable String token, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        boolean success = passwordResetService.resetPassword(token, newPassword);
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "Password reset successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid or expired token.");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
