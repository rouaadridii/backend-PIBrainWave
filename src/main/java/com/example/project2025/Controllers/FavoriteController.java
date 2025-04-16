package com.example.project2025.Controllers;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.Favorite;
import com.example.project2025.Entities.User;
import com.example.project2025.Services.FavoriteService;
import com.example.project2025.Repositories.UserRepository;
import com.example.project2025.Repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*")

public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @PostMapping("/add")
    public ResponseEntity<Favorite> addFavorite(@RequestParam Long userId, @RequestParam Long articleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with ID: " + articleId));

        Favorite favorite = favoriteService.addFavorite(user, article);
        return ResponseEntity.ok(favorite); // Ensure this is a valid JSON object
    }


    @GetMapping("/user/{userId}/favorites")
    public ResponseEntity<List<Favorite>> getFavorites(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
        List<Favorite> favorites = favoriteService.getFavoritesByUser(user);
        return ResponseEntity.ok(favorites);
    }
    @GetMapping("/user/{userId}/articles")
    public ResponseEntity<List<Article>> getFavoriteArticlesByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
        List<Favorite> favorites = favoriteService.getFavoritesByUser(user);
        List<Article> articles = favorites.stream()
                .map(Favorite::getArticle)
                .toList(); // Collect articles from favorites
        return ResponseEntity.ok(articles);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        favoriteService.removeFavorite(id);
        return ResponseEntity.noContent().build();
    }

}