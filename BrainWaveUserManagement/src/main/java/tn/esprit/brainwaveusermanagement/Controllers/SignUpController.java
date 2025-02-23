    package tn.esprit.brainwaveusermanagement.Controllers;

    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import tn.esprit.brainwaveusermanagement.Entities.Person;
    import tn.esprit.brainwaveusermanagement.dto.SignupRequest;
    import tn.esprit.brainwaveusermanagement.Services.CloudinaryService;
    import tn.esprit.brainwaveusermanagement.Services.PersonService;
    import tn.esprit.brainwaveusermanagement.Utils.PasswordEncoderUtil;

    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/auth")
    @CrossOrigin(origins = "http://localhost:4200")
    public class SignUpController {

        @Autowired
        private PersonService personService;
        @Autowired
        private CloudinaryService cloudinaryService;

        @PostMapping("/signup")
        public ResponseEntity<?> signup(@Valid @ModelAttribute SignupRequest signupRequest,
                                             @RequestParam(value = "picture", required = false) MultipartFile picture,
                                             @RequestParam(value = "cv", required = false) MultipartFile cv,
                                             @RequestParam(value = "diploma", required = false) MultipartFile diploma)
        {
            try {
                // Log received signup request
                System.out.println("Signup request received: " + signupRequest.getEmail());

                // Check if email or CIN already exists
                if (personService.existsByEmail(signupRequest.getEmail())) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Email or CIN already exists!");
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                // Parse birthDate
                Date birthDate = null;
                if (signupRequest.getBirthDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    birthDate = sdf.parse(signupRequest.getBirthDate());  // Parse the date string
                }

                // Create a new Person
                Person person = new Person();
                person.setCin(signupRequest.getCin());
                person.setName(signupRequest.getName());
                person.setSurname(signupRequest.getSurname());
                person.setEmail(signupRequest.getEmail());
                person.setPassword(PasswordEncoderUtil.encodePassword(signupRequest.getPassword())); // Encrypt password
                person.setRole(signupRequest.getRole());

                // Set nullable fields
                if (birthDate != null) {
                    person.setBirthDate(birthDate);  // Set birthDate in the Person entity
                }

                if (signupRequest.getPhoneNumber() != null) {
                    person.setPhoneNumber(signupRequest.getPhoneNumber());
                }
                if (signupRequest.getAddress() != null) {
                    person.setAddress(signupRequest.getAddress());
                }
                if (signupRequest.getLevel() != null) {
                    person.setLevel(signupRequest.getLevel());
                }


                if (picture != null && !picture.isEmpty()) {
                    System.out.println("Uploading picture...");
                    String pictureUrl = cloudinaryService.uploadImage(picture);
                    person.setPicture(pictureUrl);
                }
                if (cv != null && !cv.isEmpty()) {
                    System.out.println("Uploading CV...");
                    String cvUrl = cloudinaryService.uploadImage(cv);
                    person.setCv(cvUrl); // Set CV URL to the proper field
                }

                if (diploma != null && !diploma.isEmpty()) {
                    System.out.println("Uploading Diploma...");
                    String diplomaUrl = cloudinaryService.uploadImage(diploma);
                    person.setDiploma(diplomaUrl); // Set Diploma URL to the proper field
                }


                // Save the person in the database
                personService.savePerson(person);

                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "User registered successfully!");
                return ResponseEntity.ok(successResponse);

            } catch (Exception e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        }
    }
