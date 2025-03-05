package tn.esprit.brainwaveusermanagement.Services;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import com.sendgrid.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class EmailService {

    private Dotenv dotenv = Dotenv.load();
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private Map<String, String> verificationCodes = new HashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        String fromEmail = dotenv.get("EMAIL_USERNAME");
        String fromPassword = dotenv.get("EMAIL_PASSWORD");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        if (mailSender instanceof JavaMailSenderImpl) {
            JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
            mailSenderImpl.setUsername(fromEmail);
            mailSenderImpl.setPassword(fromPassword);
        }

        // Gmail's SMTP server will be used to send the email
        try {
            logger.info("Attempting to send email to: {}", to);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending email to: {}", to, e);
        }
    }

    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }

    public void storeVerificationCode(String email, String code) {
        verificationCodes.put(email, code);
    }

    public String getStoredVerificationCode(String email) {
        return verificationCodes.get(email);
    }

    public void removeVerificationCode(String email) {
        verificationCodes.remove(email);
    }

    public void sendVerificationEmail(String to) {
        String code = generateVerificationCode();
        storeVerificationCode(to, code);

        String subject = "Email Verification";
        String body = "Your verification code is: " + code;

        sendEmail(to, subject, body); // Use your existing sendEmail method
    }


}
