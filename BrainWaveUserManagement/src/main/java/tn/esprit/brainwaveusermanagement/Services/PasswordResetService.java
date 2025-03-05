package tn.esprit.brainwaveusermanagement.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Utils.PasswordEncoderUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordResetService {
    @Autowired
    private EmailService emailService;
    @Autowired
    private PersonService personService;

    private Map<String, String> resetTokens = new HashMap<>();

    // Send password reset email
    public void sendPasswordResetEmail(String email) {

        if (!personService.existsByEmail(email)) {
            return;
        }
        // Generate a random token
        String token = UUID.randomUUID().toString();

        // Store token temporarily (in-memory, use DB in production)
        resetTokens.put(token, email);

        // Construct the reset URL
        String resetUrl = "http://localhost:4200/reset-password/" + token;

        String subject = "Password Reset Request";
        String body = "To reset your password, click the following link: " + resetUrl;

        try {
            emailService.sendEmail(email, subject, body);  // Using the existing sendEmail method in EmailService
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    // Validate the token
    public String validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return resetTokens.get(token);
    }

    // Reset password logic (change password)
    public boolean resetPassword(String token, String newPassword) {
        String email = validateToken(token);
        if (email != null) {
            Person person = personService.findUserByEmail(email);
            if (person != null) {
                person.setPassword(PasswordEncoderUtil.encodePassword(newPassword));
                personService.savePerson(person);
                resetTokens.remove(token);
                return true;
            }
        }
        return false;
    }
}
