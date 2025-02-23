package tn.esprit.brainwaveusermanagement.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Entities.UserStatus;
import tn.esprit.brainwaveusermanagement.Services.PersonService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private PersonService personService;


    @GetMapping("/pending-teachers")
    public ResponseEntity<List<Person>> getPendingTeachers() {
        List<Person> pendingTeachers = personService.findByStatus(UserStatus.PENDING);
        return ResponseEntity.ok(pendingTeachers);
    }

    // Approve a teacher
    @PutMapping("/approve-teacher/{id}")
    public ResponseEntity<?> approveTeacher(@PathVariable Long id) {
        Person teacher = personService.findById(id);
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
    @DeleteMapping("/reject-teacher/{id}")
    public ResponseEntity<?> rejectTeacher(@PathVariable Long id) {
        Person teacher = personService.findById(id);
        if (teacher == null || teacher.getStatus() != UserStatus.PENDING) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Teacher not found or already rejected.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        // Delete the teacher's account
        personService.deletePerson(teacher);
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Teacher rejected and account deleted.");
        return ResponseEntity.ok(successResponse);
    }
}
