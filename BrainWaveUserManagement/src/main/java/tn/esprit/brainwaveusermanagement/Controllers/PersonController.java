    package tn.esprit.brainwaveusermanagement.Controllers;

    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import tn.esprit.brainwaveusermanagement.Entities.Person;
    import tn.esprit.brainwaveusermanagement.Services.CloudinaryService;
    import tn.esprit.brainwaveusermanagement.Services.PersonService;
    import tn.esprit.brainwaveusermanagement.dto.SignupRequest;
    import tn.esprit.brainwaveusermanagement.dto.UpdateProfileRequest;

    import java.io.IOException;
    import java.nio.file.attribute.UserPrincipal;
    import java.util.HashMap;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/user")
    public class PersonController {
        @Autowired
        private PersonService personService;

        @GetMapping("/profile")
        public ResponseEntity<Person> getProfile(@RequestHeader("Authorization") String token) {
            String extractedToken = token.replace("Bearer ", "");
            Person user = personService.getAuthenticatedUser(extractedToken);
            return ResponseEntity.ok(user);
        }

        @PutMapping("/update-profile")
        public ResponseEntity<?> updateProfile(
                @RequestHeader("Authorization") String token,
                @RequestBody @Valid UpdateProfileRequest updateRequest) {
            try {
                Person updatedUser = personService.updateUserProfile(token, updateRequest);
                return ResponseEntity.ok(Map.of("message", "Profile updated successfully!", "user", updatedUser));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Error updating profile: " + e.getMessage()));
            }
        }

    }
