package tn.esprit.brainwaveusermanagement.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Entities.UserStatus;
import tn.esprit.brainwaveusermanagement.Repositories.PersonRepository;
import tn.esprit.brainwaveusermanagement.Services.*;
import tn.esprit.brainwaveusermanagement.dto.LoginRequest;
import tn.esprit.brainwaveusermanagement.Utils.JwtUtils;

import java.util.HashMap;
import java.util.List;
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
    private final EmailService emailService;
    private final PersonService personService;
    @Autowired
    private UserService userService;

    public LoginController(AuthenticationManager authenticationManager, PersonRepository personRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, ReCaptchaService reCaptchaService, PasswordResetService passwordResetService, EmailService emailService, PersonService personService) {
        this.authenticationManager = authenticationManager;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.reCaptchaService=reCaptchaService;
        this.passwordResetService=passwordResetService;
        this.emailService=emailService;
        this.personService = personService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("Login attempt for email: " + request.getEmail());
        Person person = personRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + person.getEmail());

        if (person.isBanned()) {
            // If the account is banned
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account has been banned. Please contact support.");
        }

        if (person.getStatus() == UserStatus.PENDING) {
            // If the account is pending approval
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
        System.out.println("Password valid for user: " + request.getEmail());

        // Send 2FA verification email
        emailService.sendVerificationEmail(person.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA code sent to your email. Please verify.");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/face-login")
    public ResponseEntity<?> faceLogin(@RequestBody Map<String, List<Float>> requestBody) {
        List<Float> receivedDescriptor = requestBody.get("faceDescriptor");

        if (receivedDescriptor == null || receivedDescriptor.size() != 128) {
            return new ResponseEntity<>(Map.of("error", "Invalid face descriptor format."), HttpStatus.BAD_REQUEST);
        }

        try {
            Person authenticatedUser = personService.authenticateWithFace(receivedDescriptor);
            if (authenticatedUser != null) {
                UserDetails userDetails = userService.loadUserByUsername(authenticatedUser.getEmail());
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Generate JWT token using the Authentication object
                String token = jwtUtils.generateJwtToken(authentication);

                Map<String, String> response = new HashMap<>();
                response.put("token", "Bearer " + token);
                response.put("email", authenticatedUser.getEmail());
                return ResponseEntity.ok(response);
            } else {
                return new ResponseEntity<>(Map.of("error", "Face not recognized."), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Error during face authentication: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FACode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        // Retrieve the stored code for the user
        String storedCode = emailService.getStoredVerificationCode(email);
        if (storedCode != null && storedCode.equals(code)) {
            // Code is correct, authenticate the user and generate JWT
            Person person = personRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.get("password"))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtils.generateJwtToken(authentication);

            Map<String, String> response = new HashMap<>();
            response.put("token", "Bearer " + token);
            response.put("email", email);

            emailService.removeVerificationCode(email);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid 2FA code.");
        }
    }

}
