    package tn.esprit.brainwaveusermanagement.Controllers;

    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.*;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.client.RestTemplate;
    import org.springframework.web.multipart.MultipartFile;
    import tn.esprit.brainwaveusermanagement.Entities.Person;
    import tn.esprit.brainwaveusermanagement.Entities.RoleType;
    import tn.esprit.brainwaveusermanagement.Entities.UserStatus;
    import tn.esprit.brainwaveusermanagement.Services.EmailService;
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
        @Autowired
        private EmailService emailService;

        @PostMapping("/signup")
        public ResponseEntity<?> signup(@Valid @ModelAttribute SignupRequest signupRequest,
                                             @RequestParam(value = "picture", required = false) MultipartFile picture,
                                             @RequestParam(value = "cv", required = false) MultipartFile cv,
                                             @RequestParam(value = "diploma", required = false) MultipartFile diploma)
        {
            try {
                System.out.println("Signup request received: " + signupRequest.getEmail());

                // Check if email or CIN already exists
                if (personService.existsByEmail(signupRequest.getEmail())) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Email or CIN already exists!");
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                if (emailService.getStoredVerificationCode(signupRequest.getEmail()) != null) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Email not verified. Please verify your email before signing up.");
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

                // Set status based on role
                if (signupRequest.getRole() == RoleType.TEACHER) {
                    person.setStatus(UserStatus.PENDING);
                } else if (signupRequest.getRole() == RoleType.STUDENT) {
                    person.setStatus(UserStatus.APPROVED);
                }

                if (birthDate != null) {
                    person.setBirthDate(birthDate);
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

                personService.savePerson(person);

                // Send welcome email after successful registration
                String subject = "Welcome to Our Platform!";
                String name = person.getName();
                String surname = person.getSurname();
                String body =name +" " + surname +"\n Thank you for joining! We're excited to have you with us.";


                // If the role is TEACHER and the status is PENDING
                if (person.getRole() == RoleType.TEACHER) {
                    body += "\nYour account is pending approval from an admin.";
                }

                emailService.sendEmail(person.getEmail(), subject, body);

                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "User registered successfully!");
                return ResponseEntity.ok(successResponse);

            } catch (Exception e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        }

        @PostMapping("/verify-email")
        public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> payload) {
            String email = payload.get("email");
            String code = payload.get("code");

            if (email == null || code == null) {
                return ResponseEntity.badRequest().body("Email and code are required.");
            }

            String storedCode = emailService.getStoredVerificationCode(email);

            if (storedCode == null || !storedCode.equals(code)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid verification code.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            emailService.removeVerificationCode(email);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Email verified successfully.");
            return ResponseEntity.ok(successResponse);
        }

        @PostMapping("/send-verification-email")
        public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> payload){
            String email = payload.get("email");
            emailService.sendVerificationEmail(email);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Verification email sent.");
            return ResponseEntity.ok(successResponse);
        }

    }
