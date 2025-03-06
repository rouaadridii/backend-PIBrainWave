package tn.esprit.brainwaveusermanagement.Services;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import com.sendgrid.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class EmailService {

    private Dotenv dotenv = Dotenv.load();
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private Map<String, VerificationData> verificationDataMap = new HashMap<>();

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

    public void storeVerificationCode(String email, String code, LocalDateTime expirationTime) {
        verificationDataMap.put(email, new VerificationData(code, expirationTime));
    }
    public String getStoredVerificationCode(String email) {

        VerificationData data = verificationDataMap.get(email);
        if (data != null && LocalDateTime.now().isBefore(data.expirationTime)) {
            return data.code;
        }
        return null;
    }

    public void removeVerificationCode(String email) {
        verificationDataMap.remove(email);
    }
    public void sendVerificationEmail(String to) {
        String code = generateVerificationCode();
        LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(30);
        storeVerificationCode(to, code, expirationTime);

        String subject = "Email Verification";
        String body = "Your verification code is: " + code +"\n This code will expire at: "+ expirationTime;

        sendEmail(to, subject, body);
    }

    private static class VerificationData {
        String code;
        LocalDateTime expirationTime;

        VerificationData(String code, LocalDateTime expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }
    }


}
