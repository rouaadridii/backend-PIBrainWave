package tn.esprit.brainwaveusermanagement.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Entities.UserStatus;
import tn.esprit.brainwaveusermanagement.Services.PersonService;
import tn.esprit.brainwaveusermanagement.Services.EmailService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private PersonService personService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/users")
    public ResponseEntity<List<Person>> getAllUsersExceptAdmins() {
        List<Person> users = personService.findAllExceptAdmin();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending-teachers")
    public ResponseEntity<List<Person>> getPendingTeachers() {
        List<Person> pendingTeachers = personService.findByStatus(UserStatus.PENDING);
        return ResponseEntity.ok(pendingTeachers);
    }

    // Approve a teacher
    @PutMapping("/approve-teacher/{cin}")
    public ResponseEntity<?> approveTeacher(@PathVariable String cin) {
        Person teacher = personService.findBycin(cin);
        if (teacher == null || teacher.getStatus() != UserStatus.PENDING) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Teacher not found or already approved.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        teacher.setStatus(UserStatus.APPROVED);
        personService.savePerson(teacher);
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Teacher approved successfully!");
        return ResponseEntity.ok(successResponse);
    }

    //reject a teacher
    @DeleteMapping("/reject-teacher/{cin}")
    public ResponseEntity<?> rejectTeacher(@PathVariable String cin) {
        Person teacher = personService.findBycin(cin);
        if (teacher == null || teacher.getStatus() != UserStatus.PENDING) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Teacher not found or already rejected.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Send rejection email
        String to = teacher.getEmail(); // Assuming Person has an getEmail() method
        String subject = "Application Rejection";
        String name = teacher.getName();
        String surname = teacher.getSurname();
        String body = "\nDear " + name +" "+ surname + " \nWe regret to inform you that your application to become a teacher has been rejected. We appreciate your interest and wish you the best in your future endeavors.";
        System.out.println("Sending rejection email to: " + to); // Add this line
        try {
            emailService.sendEmail(to, subject, body); // Corrected line: Call the EmailService
            // Delete the teacher's account
            personService.deletePerson(teacher);
        } catch (Exception e) {
            // Handle email sending error (e.g., log it)
            System.err.println("Error sending rejection email: " + e.getMessage());
        }


        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Teacher rejected and account deleted.");
        return ResponseEntity.ok(successResponse);
    }

    @PutMapping("/ban/{email}")
    public ResponseEntity<Person> banUser(@PathVariable String email) {
        Person bannedUser = personService.banUser(email);
        return ResponseEntity.ok(bannedUser);
    }

    @PutMapping("/unban/{email}")
    public ResponseEntity<Person> unbanUser(@PathVariable String email) {
        Person unbannedUser = personService.unbanUser(email);
        return ResponseEntity.ok(unbannedUser);
    }
}
