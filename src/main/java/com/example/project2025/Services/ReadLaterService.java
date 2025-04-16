package com.example.project2025.Services;

import com.example.project2025.Controllers.NotificationSSEController;
import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.ReadLater;
import com.example.project2025.Entities.User;
import com.example.project2025.Repositories.ArticleRepository;
import com.example.project2025.Repositories.ReadLaterRepository;
import com.example.project2025.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ReadLaterService {

    @Autowired
    private ReadLaterRepository readLaterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;
    private final NotificationSSEController notificationSSEController;


    @Transactional
    public ReadLater addReadLater(Long userId, Long articleId, LocalDateTime reminderDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with ID: " + articleId));

        // Check if already exists
        Optional<ReadLater> existing = readLaterRepository.findByUser_IdAndArticle_Id(userId, articleId);
        if (existing.isPresent()) {
            ReadLater existingReadLater = existing.get();
            existingReadLater.setReminderDate(reminderDate);
            existingReadLater.setNotified(false);
            return readLaterRepository.save(existingReadLater);
        }

        // Create new entry
        ReadLater readLater = new ReadLater();
        readLater.setUser(user);
        readLater.setArticle(article);
        readLater.setReminderDate(reminderDate);
        return readLaterRepository.save(readLater);
    }

    public List<ReadLater> getReadLaterByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return readLaterRepository.findByUser(user);
    }

    @Transactional
    public void removeReadLater(Long id) {
        readLaterRepository.deleteById(id);
    }

    @Transactional
    public void removeReadLaterByUserAndArticle(Long userId, Long articleId) {
        Optional<ReadLater> readLater = readLaterRepository.findByUser_IdAndArticle_Id(userId, articleId);
        readLater.ifPresent(readLaterRepository::delete);
    }

    @Autowired
    public ReadLaterService(ReadLaterRepository readLaterRepository, UserRepository userRepository, ArticleRepository articleRepository, ArticleService articleService,@Lazy NotificationSSEController notificationSSEController) {
        this.readLaterRepository = readLaterRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.articleService = articleService;
        this.notificationSSEController = notificationSSEController;
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<ReadLater> pendingReminders = readLaterRepository.findPendingReminders(now);

        for (ReadLater reminder : pendingReminders) {
            reminder.setNotified(true);
            readLaterRepository.save(reminder);
            // Send SSE notification to the user
            notificationSSEController.sendNotificationToUser(reminder.getUser().getId(), "Un article que vous avez mis de côté est prêt à être consulté !");
        }
    }
}
