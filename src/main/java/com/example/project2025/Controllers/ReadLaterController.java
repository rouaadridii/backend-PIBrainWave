package com.example.project2025.Controllers;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.ReadLater;
import com.example.project2025.Services.ArticleService;
import com.example.project2025.Services.ReadLaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/readlater")
@CrossOrigin(origins = "*")
public class ReadLaterController {

    @Autowired
    private ReadLaterService readLaterService;

    @Autowired
    private ArticleService articleService;

    @PostMapping("/add")
    public ResponseEntity<ReadLater> addReadLater(
            @RequestParam Long userId,
            @RequestParam Long articleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reminderDate) {

        // If no reminder date is provided, set it to 24 hours from now
        if (reminderDate == null) {
            reminderDate = LocalDateTime.now().plusHours(24);
        }

        ReadLater readLater = readLaterService.addReadLater(userId, articleId, reminderDate);
        return ResponseEntity.ok(readLater);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReadLater>> getReadLaterByUser(@PathVariable Long userId) {
        List<ReadLater> readLaterList = readLaterService.getReadLaterByUser(userId);
        return ResponseEntity.ok(readLaterList);
    }

    @GetMapping("/user/{userId}/articles")
    public ResponseEntity<List<Article>> getReadLaterArticlesByUser(@PathVariable Long userId) {
        List<ReadLater> readLaterList = readLaterService.getReadLaterByUser(userId);
        List<Article> articles = readLaterList.stream()
                .map(ReadLater::getArticle)
                .collect(Collectors.toList());
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<ReadLater>> getPendingReadLaterByUser(@PathVariable Long userId) {
        List<ReadLater> allReadLater = readLaterService.getReadLaterByUser(userId);
        List<ReadLater> pendingReadLater = allReadLater.stream()
                .filter(rl -> !rl.isNotified() && LocalDateTime.now().isAfter(rl.getReminderDate()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingReadLater);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeReadLater(@PathVariable Long id) {
        readLaterService.removeReadLater(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeReadLaterByUserAndArticle(
            @RequestParam Long userId,
            @RequestParam Long articleId) {
        readLaterService.removeReadLaterByUserAndArticle(userId, articleId);
        return ResponseEntity.ok(Map.of("message", "ReadLater entry removed successfully"));
    }
}
