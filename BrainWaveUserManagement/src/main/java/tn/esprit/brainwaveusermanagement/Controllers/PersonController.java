    package tn.esprit.brainwaveusermanagement.Controllers;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import tn.esprit.brainwaveusermanagement.Entities.Person;
    import tn.esprit.brainwaveusermanagement.Services.CloudinaryService;
    import tn.esprit.brainwaveusermanagement.Services.PersonService;

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
            String extractedToken = token.replace("Bearer ", ""); // Remove 'Bearer ' prefix if present
            Person user = personService.getAuthenticatedUser(extractedToken);
            return ResponseEntity.ok(user);
        }
    }
