package com.example.project2025.Controllers;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.ArticleCategorie;
import com.example.project2025.Entities.PublicationStatus;
import com.example.project2025.Services.ArticleService;
import com.example.project2025.Entities.Tag;
import com.example.project2025.Services.ChatbotService;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/articles")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ChatbotService chatbotService;
    @PostMapping(path = "/ajouter",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createArticle(
            @RequestParam("id") Long userId,
            @RequestParam("title") String title,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "categorie") ArticleCategorie categorie,
            @RequestParam(value = "status", required = false) Boolean status,
            @RequestParam(value = "publicationStatus", required = false) PublicationStatus publicationStatus,
            @RequestParam(value = "views", required = false) Integer views,
            @RequestParam(value = "numberShares", required = false) Integer numberShares,
            @RequestParam(value = "picture", required = false) MultipartFile pictureFile,
            @RequestParam(value = "video", required = false) MultipartFile videoFile,
            @RequestParam(value = "pdf", required = false) MultipartFile pdfFile,
            @RequestParam(value = "tags", required = false) List<String> tagNames // Add this line to receive the tags
    ) {
        try {
            Article article = new Article();
            article.setTitle(title);
            article.setDate(Optional.ofNullable(dateStr)
                    .filter(s -> !s.isEmpty())
                    .map(LocalDate::parse)
                    .orElse(LocalDate.now()));
            article.setCategorie(categorie);
            article.setStatus(Optional.ofNullable(status).orElse(false));
            article.setPublicationStatus(Optional.ofNullable(publicationStatus).orElse(PublicationStatus.DRAFT));
            article.setViews(Optional.ofNullable(views).orElse(0));
            article.setNumberShares(Optional.ofNullable(numberShares).orElse(0));

            Article savedArticle = articleService.createArticle(article, userId, pictureFile, videoFile, pdfFile);

            if (tagNames != null && !tagNames.isEmpty()) {
                Article updatedArticleWithTags = articleService.addTagsToArticle(savedArticle.getId(), tagNames);
                return ResponseEntity.status(HttpStatus.CREATED).body(updatedArticleWithTags);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            logger.error("Erreur lors de l'upload des fichiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Une erreur est survenue lors de l'upload des fichiers",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'article", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Une erreur est survenue lors de la création de l'article",
                    "message", e.getMessage()
            ));
        }
    }


    @PutMapping(path = "/modifier/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateArticle(@PathVariable Long id,
                                                @Valid @ModelAttribute Article article,
                                                @RequestParam(value = "picture", required = false) MultipartFile pictureFile,
                                                @RequestParam(value = "video", required = false) MultipartFile videoFile,
                                                @RequestParam(value = "pdf", required = false) MultipartFile pdfFile) {
        try {
            Article updatedArticle = articleService.updateArticle(id, article, pictureFile, videoFile, pdfFile);
            return ResponseEntity.ok(updatedArticle);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            logger.error("Erreur lors de l'upload des fichiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Une erreur est survenue lors de l'upload des fichiers",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'article", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Une erreur est survenue lors de la mise à jour de l'article",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateArticleStatus(@PathVariable Long id, @RequestParam String newStatus) {
        try {
            PublicationStatus status = PublicationStatus.valueOf(newStatus.toUpperCase());
            Article updatedArticle = articleService.transitionArticleStatus(id, status);
            return ResponseEntity.ok(updatedArticle);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", "Statut d'article invalide: " + newStatus));
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du statut de l'article", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(Map.of(
                    "error", "Erreur serveur lors de la mise à jour du statut.",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<java.util.List<Article>> getArticlesByStatus(@PathVariable String status) {
        try {
            PublicationStatus publicationStatus = PublicationStatus.valueOf(status.toUpperCase());
            java.util.List<Article> articles = articleService.getArticlesByStatus(publicationStatus);
            return ResponseEntity.ok(articles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.emptyList()); // Or a simple error string
        }
    }

    @GetMapping
    public ResponseEntity<java.util.List<Article>> getAllArticles() {
        java.util.List<Article> articles = articleService.getAllArticles();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        java.util.Optional<Article> article = articleService.getArticleById(id);
        return article.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getArticleWithRessources/{id}")
    public ResponseEntity<Article> getArticleWithRessources(@PathVariable Long id) {
        try {
            Article article = articleService.getArticleWithRessources(id);
            return ResponseEntity.ok(article);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/increment-view/{id}")
    public ResponseEntity<Article> incrementView(@PathVariable Long id) {
        Optional<Article> updatedArticle = articleService.incrementView(id);
        return updatedArticle.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/top-5-viewed")
    public List<Article> getTop5MostViewedArticles() {
        return articleService.getTop5MostViewedArticles();
    }

    @GetMapping("/published")
    public ResponseEntity<List<Article>> getPublishedArticles() {
        return ResponseEntity.ok(articleService.getArticlesByStatus(PublicationStatus.PUBLISHED)); // Utiliser le nouveau service
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Article> approveArticle(@PathVariable Long id) {
        try {
            Article approvedArticle = articleService.approveArticle(id);
            return ResponseEntity.ok(approvedArticle);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping("/react/{articleId}")
    public ResponseEntity<Map<String, String>> reactArticle(
            @PathVariable Long articleId,
            @RequestParam Long userId,
            @RequestParam String reactionType) {
        try {
            articleService.addOrUpdateReaction(articleId, userId, reactionType);
            return ResponseEntity.ok(Map.of("message", "Reaction added/updated successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to react to article"));
        }
    }

    @DeleteMapping("/unreact/{articleId}")
    public ResponseEntity<Map<String, String>> unreactArticle(@PathVariable Long articleId, @RequestParam Long userId) {
        try {
            articleService.removeReaction(articleId, userId);
            return ResponseEntity.ok(Map.of("message", "Reaction removed successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to remove reaction"));
        }
    }

    @GetMapping("/reactions/{articleId}")
    public ResponseEntity<Map<String, Long>> getArticleReactions(@PathVariable Long articleId) {
        try {
            Map<String, Long> reactions = articleService.getReactionCounts(articleId);
            return ResponseEntity.ok(reactions);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user-reaction/{articleId}")
    public ResponseEntity<String> getUserReactionOnArticle(@PathVariable Long articleId, @RequestParam Long userId) {
        try {
            String reactionType = articleService.getUserReaction(articleId, userId);
            return ResponseEntity.ok(reactionType); // Peut être null si l'utilisateur n'a pas réagi
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<Article>> getSimilarArticles(@PathVariable Long id, @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Article> similarArticles = articleService.findSimilarArticles(id, limit);
            return ResponseEntity.ok(similarArticles);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error finding similar articles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<Object> addTagsToArticle(@PathVariable Long id, @RequestBody List<String> tagNames) {
        try {
            Article updatedArticle = articleService.addTagsToArticle(id, tagNames);
            return ResponseEntity.ok(updatedArticle);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding tags to article", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "An error occurred while adding tags to the article",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{articleId}/tags/{tagId}")
    public ResponseEntity<Object> removeTagFromArticle(@PathVariable Long articleId, @PathVariable Long tagId) {
        try {
            Article updatedArticle = articleService.removeTagFromArticle(articleId, tagId);
            return ResponseEntity.ok(updatedArticle);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing tag from article", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "An error occurred while removing tag from the article",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        try {
            List<Tag> tags = articleService.getAllTags();
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            logger.error("Error retrieving tags", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check remaining API calls for the chatbot
     */
    @GetMapping("/chatbot/limit")
    public ResponseEntity<Map<String, Object>> getChatbotLimit() {
        int remainingCalls = chatbotService.getRemainingApiCalls();
        Map<String, Object> response = Map.of(
            "dailyLimit", chatbotService.getDailyRateLimit(),
            "remainingCalls", remainingCalls,
            "isLimited", remainingCalls <= 0
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour interagir avec le chatbot concernant les articles
     */
    @PostMapping("/chatbot")
    public ResponseEntity<Map<String, Object>> chatWithBot(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            Long userId = request.containsKey("userId") ? Long.parseLong(request.get("userId")) : null;
            String context = request.getOrDefault("context", "general");

            logger.info("Chatbot request: message='{}', userId={}, context='{}'", message, userId, context);

            if (message == null || message.trim().isEmpty()) {
                logger.warn("Empty message received in chatbot request");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le message ne peut pas être vide"));
            }

            // Test si le chatbot est accessible
            boolean isAvailable = chatbotService.testChatbotAvailability();
            if (!isAvailable) {
                logger.warn("Chatbot API is not available");
                Map<String, Object> fallbackResponse = new HashMap<>();
                fallbackResponse.put("type", "text_response");
                fallbackResponse.put("message", "Je suis désolé, le service de chatbot est temporairement indisponible. Voici une réponse basique.");
                fallbackResponse.put("fallback", true);

                if (message.toLowerCase().contains("bonjour") || message.toLowerCase().contains("salut")) {
                    fallbackResponse.put("message", "Bonjour ! Comment puis-je vous aider avec vos articles aujourd'hui ?");
                } else if (message.toLowerCase().contains("aide")) {
                    fallbackResponse.put("message", "Je peux vous aider à trouver, analyser et suggérer des articles. Actuellement, je fonctionne en mode limité.");
                }

                return ResponseEntity.ok(fallbackResponse);
            }

            Map<String, Object> response = chatbotService.processMessage(message, userId, context);
            logger.info("Chatbot response type: {}", response.get("type"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing chatbot request: {}", e.getMessage(), e);
            // Réponse de secours en cas d'erreur
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "text_response");
            errorResponse.put("message", "Je suis désolé, j'ai rencontré une erreur. Essayez de poser une question simple.");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("fallback", true);
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    /**
     * Vérifier la santé du chatbot et le tester
     */
    @GetMapping("/chatbot/health")
    public ResponseEntity<Map<String, Object>> checkChatbotHealth() {
        Map<String, Object> healthStatus = new HashMap<>();

        boolean isAvailable = chatbotService.testChatbotAvailability();
        int remainingCalls = chatbotService.getRemainingApiCalls();

        healthStatus.put("available", isAvailable);
        healthStatus.put("apiKeyConfigured", chatbotService.isApiKeyConfigured());
        healthStatus.put("remainingCalls", remainingCalls);
        healthStatus.put("dailyLimit", chatbotService.getDailyRateLimit());
        healthStatus.put("fallbackEnabled", chatbotService.isFallbackEnabled());

        if (!isAvailable) {
            healthStatus.put("message", "Le service de chatbot n'est pas disponible actuellement. Vérifiez l'API key et les paramètres.");
            String testResponse = chatbotService.generateFallbackResponse("test");
            healthStatus.put("fallbackResponse", testResponse);
        } else {
            healthStatus.put("message", "Le service de chatbot est opérationnel.");
        }

        return ResponseEntity.ok(healthStatus);
    }

    /**
     * Obtenir des suggestions de contenu pour un article en cours de rédaction
     */
    @PostMapping("/chatbot/suggestion")
    public ResponseEntity<Map<String, Object>> getSuggestion(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String partialContent = request.getOrDefault("content", "");
            String category = request.getOrDefault("category", "");

            Map<String, Object> suggestions = chatbotService.getSuggestions(title, partialContent, category);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            logger.error("Error getting article suggestions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Impossible d'obtenir des suggestions",
                                "message", e.getMessage()));
        }
    }

    /**
     * Analyser un article et donner un feedback
     */
    @PostMapping("/chatbot/analyze")
    public ResponseEntity<Map<String, Object>> analyzeArticle(@RequestParam Long articleId) {
        try {
            Optional<Article> articleOpt = articleService.getArticleById(articleId);
            if (articleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Article introuvable"));
            }

            Map<String, Object> analysis = chatbotService.analyzeArticle(articleOpt.get());
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("Error analyzing article", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'analyse de l'article",
                                "message", e.getMessage()));
        }
    }
}