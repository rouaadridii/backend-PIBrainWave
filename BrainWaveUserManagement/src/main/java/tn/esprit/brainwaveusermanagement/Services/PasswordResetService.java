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

    public void sendPasswordResetEmail(String email) {

        if (!personService.existsByEmail(email)) {
            return;
        }
        String token = UUID.randomUUID().toString();

        resetTokens.put(token, email);

        String resetUrl = "http://localhost:4200/reset-password/" + token;
        String subject = "Password Reset Request";
        String body = "To reset your password, click the following link: " + resetUrl;

        try {
            emailService.sendEmail(email, subject, body);  // Using the existing sendEmail method in EmailService
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public String validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return resetTokens.get(token);
    }

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
