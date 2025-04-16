package com.example.project2025.Services;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.ArticleCategorie;
import com.example.project2025.Entities.Tag;
import com.example.project2025.Repositories.ArticleRepository;
import com.example.project2025.Repositories.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private ArticleService articleService;

    @Value("${chatbot.api.url:https://api.nlpcloud.io/v1/flan-t5-large/chatbot}")
    private String chatbotApiUrl;
    
    @Value("${chatbot.api.key:f0418841c9049f910fa8c0e6742cf8923fcc6e89}")
    private String chatbotApiKey;
    
    // Add rate limiting properties
    @Value("${chatbot.rate.limit.daily:100}")
    private int dailyRateLimit;
    
    @Value("${chatbot.api.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    // Simple counter for API calls (would be better to use a persistent store in production)
    private final Map<String, Integer> apiCallCounters = new HashMap<>();
    private LocalDate currentCounterDate = LocalDate.now();
    
    /**
     * Check if we've exceeded our API rate limit and reset counter if it's a new day
     */
    private synchronized boolean isRateLimitExceeded() {
        // Reset counter if it's a new day
        LocalDate today = LocalDate.now();
        if (!today.equals(currentCounterDate)) {
            apiCallCounters.clear();
            currentCounterDate = today;
        }
        
        String dateKey = today.toString();
        int currentCount = apiCallCounters.getOrDefault(dateKey, 0);
        return currentCount >= dailyRateLimit;
    }
    public int getDailyRateLimit() {
    return dailyRateLimit;
}
    /**
     * Increment the API call counter
     */
    private synchronized void incrementApiCallCounter() {
        String dateKey = LocalDate.now().toString();
        int currentCount = apiCallCounters.getOrDefault(dateKey, 0);
        apiCallCounters.put(dateKey, currentCount + 1);
    }
    
    // Method to make authenticated API calls to the chatbot service
    private String callChatbotApi(String message, String context) {
        // Skip external API completely and use local processing
        return generateLocalResponse(message, context);
    }
    
    /**
     * Generate a local response based on patterns in the message and context
     */
    private String generateLocalResponse(String message, String context) {
        String messageLower = message.toLowerCase();
        
        // Article recommendations
        if (messageLower.contains("recommand") || messageLower.contains("suggest") || 
            (messageLower.contains("me") && messageLower.contains("article"))) {
            
            List<Article> popular = articleService.getTop5MostViewedArticles();
            if (popular.isEmpty()) {
                return "Désolé, aucun article n'est disponible pour le moment.";
            }
            
            String articleList = popular.stream()
                .map(Article::getTitle)
                .limit(5)
                .collect(Collectors.joining(", "));
                
            return "Voici les articles les plus populaires : " + articleList + ".";
        }
        
        // Favorite creation instructions
        if ((messageLower.contains("comment") && messageLower.contains("favoris") && 
             (messageLower.contains("créer") || messageLower.contains("cree") || messageLower.contains("ajouter"))) ||
            (messageLower.contains("comment") && messageLower.contains("ajouter") && 
             (messageLower.contains("favoris") || messageLower.contains("préférés")))) {
            
            return "Pour ajouter un article aux favoris, suivez ces étapes:\n" +
                   "1. Naviguez vers la page de l'article que vous souhaitez ajouter aux favoris\n" +
                   "2. Cliquez sur l'icône d'étoile ou le bouton 'Ajouter aux favoris'\n" +
                   "3. L'article sera immédiatement ajouté à votre liste de favoris\n\n" +
                   "Vous pourrez retrouver tous vos articles favoris dans votre profil sous 'Mes favoris'.\n" +
                   "Pour accéder directement à vos favoris, utilisez le lien: /favorites/user/{votre-id}/articles";
        }
        
        // Article creation instructions
        if (messageLower.contains("comment") && messageLower.contains("article") && 
            (messageLower.contains("créer") || messageLower.contains("cree") || 
             messageLower.contains("publier") || messageLower.contains("rédiger"))) {
            
            return "Pour créer un article, suivez ces étapes détaillées:\n" +
                   "1. Accédez à l'interface de création d'article en cliquant sur le bouton 'Créer un article'\n" +
                   "2. Remplissez les champs obligatoires:\n" +
                   "   - Titre: un titre concis et informatif (obligatoire)\n" +
                   "   - Catégorie: NEWS, BLOG ou SUCCESS_STORY (obligatoire)\n" +
                   "   - Contenu: ajoutez le contenu principal de votre article via les ressources\n" +
                   "3. Ajoutez des ressources à votre article (images, vidéos, PDF, descriptions)\n" +
                   "4. Ajoutez des tags pertinents pour améliorer la visibilité de votre article\n" +
                   "5. Choisissez le statut de publication:\n" +
                   "   - DRAFT: pour sauvegarder et continuer plus tard\n" +
                   "   - PENDING_REVIEW: pour soumettre à révision\n" +
                   "   - PUBLISHED: pour publication immédiate (nécessite approbation)\n" +
                   "6. Cliquez sur 'Publier' ou 'Enregistrer' selon votre choix\n\n" +
                   "Endpoint API: POST /articles/ajouter?id={votre-id} avec le corps de la requête contenant les détails de l'article";
        }
        
        // Tag-related queries
        if (messageLower.contains("tag") || messageLower.contains("étiquette")) {
            List<Tag> allTags = tagRepository.findAll();
            if (allTags.isEmpty()) {
                return "Aucun tag n'est disponible pour le moment.";
            }
            
            String tagList = allTags.stream()
                .map(Tag::getName)
                .limit(10)
                .collect(Collectors.joining(", "));
                
            return "Voici les tags disponibles : " + tagList + ".";
        }
        
        // ReadLater queries
        if ((messageLower.contains("read") && messageLower.contains("later")) || 
            messageLower.contains("lire plus tard") || 
            (messageLower.contains("comment") && messageLower.contains("readlater"))) {
            
            return "Pour ajouter un article à lire plus tard, suivez ces étapes:\n" +
                   "1. Ouvrez l'article que vous souhaitez lire plus tard\n" +
                   "2. Cliquez sur le bouton 'Lire plus tard' en bas de l'article\n" +
                   "3. Vous pouvez optionnellement définir une date de rappel\n" +
                   "4. Confirmez en cliquant sur 'Ajouter'\n\n" +
                   "Vous pourrez retrouver tous vos articles à lire plus tard dans votre profil sous 'Mes lectures futures'.";
        }
        
        // Category-specific article queries
        if (checkForCategory(messageLower)) {
            ArticleCategorie category = getCategoryFromMessage(messageLower);
            List<Article> categoryArticles = articleRepository.findByPublishedTrueAndCategorie(category);
            
            if (categoryArticles.isEmpty()) {
                return "Aucun article de la catégorie " + category.name() + " n'est disponible pour le moment.";
            }
            
            String articleList = categoryArticles.stream()
                .map(Article::getTitle)
                .limit(5)
                .collect(Collectors.joining(", "));
                
            return "Voici les articles de la catégorie " + category.name() + ": " + articleList + ".";
        }
        
        // Article-related queries
        if ( messageLower.contains("publication")) {
            if (messageLower.contains("populaire") || messageLower.contains("vu") || messageLower.contains("plus vu")) {
                List<Article> popular = articleService.getTop5MostViewedArticles();
                if (popular.isEmpty()) {
                    return "Aucun article populaire n'est disponible pour le moment.";
                }
                
                String articleList = popular.stream()
                    .map(Article::getTitle)
                    .limit(3)
                    .collect(Collectors.joining(", "));
                    
                return "Les articles les plus populaires sont : " + articleList + ".";
            }
        }
        
        // Basic conversational responses
        if (messageLower.contains("bonjour") || messageLower.contains("salut")) {
            return "Bonjour ! Comment puis-je vous aider avec vos articles aujourd'hui ?";
        } else if (messageLower.contains("merci")) {
            return "De rien ! N'hésitez pas si vous avez d'autres questions.";
        } else if (messageLower.contains("aide") || messageLower.contains("help")) {
            return "Je peux vous aider à trouver des articles, suggérer des améliorations pour vos articles, ou répondre à vos questions. Que souhaitez-vous faire ?";
        }
        
        // Default response
        return "Je ne suis pas sûr de comprendre votre demande. Pourriez-vous préciser ce que vous recherchez concernant les articles ?";
    }
    
    /**
     * Check if the message contains a reference to an article category
     */
    private boolean checkForCategory(String message) {
        return message.contains("news") || 
               message.contains("blog") || 
               message.contains("success") || 
               message.contains("succès") || 
               message.contains("story") || 
               message.contains("catégorie");
    }
    
    /**
     * Extract the category type from the message
     */
    private ArticleCategorie getCategoryFromMessage(String message) {
        if (message.contains("news") || message.contains("actualité")) {
            return ArticleCategorie.NEWS;
        } else if (message.contains("success") || message.contains("succès") || message.contains("story")) {
            return ArticleCategorie.SUCCESS_STORY;
        } else {
            return ArticleCategorie.BLOG; // Default if not specified or unclear
        }
    }

    // Generate a fallback response when API call fails
    public String generateFallbackResponse(String message) {
        return generateLocalResponse(message, null);
    }
    
    /**
     * Traite les messages de l'utilisateur et génère une réponse appropriée
     */
    public Map<String, Object> processMessage(String message, Long userId, String context) {
        Map<String, Object> response = new HashMap<>();
        String messageLower = message.toLowerCase();
        
        // Détection d'intentions pour les recommandations d'articles
        if (messageLower.contains("recommand") || messageLower.contains("suggest") || 
            (messageLower.contains("me"))) {
            
            // Toujours renvoyer les 5 articles les plus vus pour les recommandations
            List<Article> recommendedArticles = articleService.getTop5MostViewedArticles();
            
            if (recommendedArticles.isEmpty()) {
                response.put("type", "text_response");
                response.put("message", "Désolé, aucun article n'est disponible pour le moment.");
            } else {
                response.put("type", "recommendations");
                response.put("message", "Voici les articles qui pourraient vous intéresser :");
                response.put("articles", recommendedArticles);
            }
        } 
        else if (messageLower.contains("recherche") || messageLower.contains("trouver")) {
            // Recherche d'articles
            List<Article> searchResults = searchArticles(message);
            response.put("type", "search_results");
            response.put("message", "Voici les résultats de votre recherche :");
            response.put("articles", searchResults);
        }

        else if (messageLower.contains("tag") || messageLower.contains("catégorie") || 
                 messageLower.contains("étiquette") || messageLower.contains("classe")) {
            
            // Récupérer TOUS les tags au lieu de filtrer par mots-clés
            List<Tag> allTags = tagRepository.findAll();
            
            if (allTags.isEmpty()) {
                response.put("type", "text_response");
                response.put("message", "Aucun tag n'est disponible pour le moment.");
            } else {
                response.put("type", "tags_info");
                response.put("message", "Voici les tags disponibles :");
                response.put("tags", allTags);
            }
        } 
        // Instructions pour les favoris
        else if ((messageLower.contains("comment") && messageLower.contains("favoris")) || 
                 (messageLower.contains("ajout") && messageLower.contains("favoris"))) {
            
            response.put("type", "text_response");
            response.put("message", "Pour ajouter un article aux favoris, suivez ces étapes:\n" +
                   "1. Naviguez vers la page de l'article que vous souhaitez ajouter aux favoris\n" +
                   "2. Cliquez sur l'icône d'étoile ou le bouton 'Ajouter aux favoris'\n" +
                   "3. L'article sera immédiatement ajouté à votre liste de favoris\n\n" +
                   "Vous pourrez retrouver tous vos articles favoris dans votre profil sous 'Mes favoris'.\n" +
                   "Pour accéder directement à vos favoris, utilisez le lien: /favorites/user/{votre-id}/articles");
        }
        // Instructions pour ReadLater
        else if ((messageLower.contains("read") && messageLower.contains("later")) || 
                 messageLower.contains("lire plus tard") || 
                 (messageLower.contains("comment") && messageLower.contains("readlater"))) {
            
            response.put("type", "text_response");
            response.put("message", "Pour ajouter un article à lire plus tard, suivez ces étapes:\n" +
                   "1. Ouvrez l'article que vous souhaitez lire plus tard\n" +
                   "2. Cliquez sur le bouton 'Lire plus tard' en bas de l'article\n" +
                   "3. Vous pouvez optionnellement définir une date de rappel\n" +
                   "4. Confirmez en cliquant sur 'Ajouter'\n\n" +
                   "Vous pourrez retrouver tous vos articles à lire plus tard dans votre profil sous 'Mes lectures futures'.");
        }
        // Instructions détaillées pour la création d'articles
        else if (messageLower.contains("comment") && messageLower.contains("article") && 
                (messageLower.contains("créer") || messageLower.contains("cree") || 
                 messageLower.contains("publier") || messageLower.contains("rédiger"))) {
            
            response.put("type", "text_response");
            response.put("message", "Pour créer un article, suivez ces étapes détaillées:\n" +
                   "1. Accédez à l'interface de création d'article en cliquant sur le bouton 'Créer un article'\n" +
                   "2. Remplissez les champs obligatoires:\n" +
                   "   - Titre: un titre concis et informatif (obligatoire)\n" +
                   "   - Catégorie: NEWS, BLOG ou SUCCESS_STORY (obligatoire)\n" +
                   "   - Contenu: ajoutez le contenu principal de votre article via les ressources\n" +
                   "3. Ajoutez des ressources à votre article (images, vidéos, PDF, descriptions)\n" +
                   "4. Ajoutez des tags pertinents pour améliorer la visibilité de votre article\n" +
                   "5. Choisissez le statut de publication:\n" +
                   "   - DRAFT: pour sauvegarder et continuer plus tard\n" +
                   "   - PENDING_REVIEW: pour soumettre à révision\n" +
                   "   - PUBLISHED: pour publication immédiate (nécessite approbation)\n" +
                   "6. Cliquez sur 'Publier' ou 'Enregistrer' selon votre choix" );
        }
        // Articles par catégorie
        else if (checkForCategory(messageLower)) {
            ArticleCategorie category = getCategoryFromMessage(messageLower);
            List<Article> categoryArticles = articleRepository.findByPublishedTrueAndCategorie(category);
            
            if (categoryArticles.isEmpty()) {
                response.put("type", "text_response");
                response.put("message", "Aucun article de la catégorie " + category.name() + " n'est disponible pour le moment.");
            } else {
                response.put("type", "category_results");
                response.put("message", "Voici les articles de la catégorie " + category.name() + ":");
                response.put("articles", categoryArticles);
                response.put("category", category.name());
            }
        }
        else {
            // Use the local response for general queries
            String chatbotResponse = generateLocalResponse(message, context);
            response.put("type", "text_response");
            response.put("message", chatbotResponse);
        }
        
        return response;
    }
    
    /**
     * Fournit des suggestions pour la rédaction d'un article
     */
    public Map<String, Object> getSuggestions(String title, String partialContent, String category) {
        Map<String, Object> suggestions = new HashMap<>();
        
        // Suggest tags based on the title and content
        List<String> suggestedTags = suggestTags(title + " " + partialContent);
        suggestions.put("tags", suggestedTags);
        
        // Suggest content improvements
        String contentSuggestion = suggestContentImprovements(title, partialContent, category);
        suggestions.put("content_improvements", contentSuggestion);
        
        // Find similar articles for reference
        List<Article> similarArticles = findSimilarArticlesByContent(title, partialContent);
        suggestions.put("similar_articles", similarArticles);
        
        return suggestions;
    }
    
    /**
     * Analyse un article et fournit un feedback
     */
    public Map<String, Object> analyzeArticle(Article article) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Analyse de la lisibilité
        String readabilityScore = analyzeReadability(article.getTitle(), getArticleContent(article));
        analysis.put("readability", readabilityScore);
        
        // Suggestions d'amélioration
        String improvementSuggestions = suggestImprovements(article);
        analysis.put("improvement_suggestions", improvementSuggestions);
        
        // Suggestions de tags supplémentaires
        List<String> additionalTags = suggestAdditionalTags(article);
        analysis.put("suggested_tags", additionalTags);
        
        return analysis;
    }
    
    // Méthodes privées d'implémentation
    
    private List<Article> getRecommendedArticles(String message) {
        // Exemple d'implémentation simple - dans la vraie vie, utilisez un algorithme de recommandation plus sophistiqué
        List<Article> allArticles = articleRepository.findByPublishedTrue();
        
        // Extraire des mots-clés de la requête
        List<String> keywords = extractKeywords(message);
        
        // Filtrer les articles en fonction des mots-clés
        return allArticles.stream()
            .filter(article -> isRelevantToKeywords(article, keywords))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private List<Article> searchArticles(String message) {
        List<String> keywords = extractKeywords(message);
        List<Article> allArticles = articleRepository.findByPublishedTrue();
        
        return allArticles.stream()
            .filter(article -> isRelevantToKeywords(article, keywords))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    private List<Tag> getRelevantTags(String message) {
        // Pour plus de simplicité, on retourne maintenant tous les tags
        // quand l'utilisateur demande des tags
        return tagRepository.findAll();
    }
    
    private String generateChatResponse(String message, String context) {
        return callChatbotApi(message, context);
    }
    
    private List<String> extractKeywords(String message) {
        // Simple keyword extraction - in real implementation use NLP library
        return Arrays.stream(message.toLowerCase().split("\\s+"))
            .filter(word -> word.length() > 3)
            .filter(word -> !Arrays.asList("avec", "pour", "dans", "comment", "quoi", "quel", "quelle").contains(word))
            .collect(Collectors.toList());
    }
    
    private boolean isRelevantToKeywords(Article article, List<String> keywords) {
        String titleLower = article.getTitle().toLowerCase();
        
        // Check if the article tags match any keywords
        boolean hasMatchingTags = article.getTags() != null && 
            article.getTags().stream()
                .anyMatch(tag -> keywords.stream()
                    .anyMatch(k -> tag.getName().toLowerCase().contains(k)));
        
        // Check if the title contains any keywords
        boolean titleContainsKeywords = keywords.stream()
            .anyMatch(titleLower::contains);
            
        return hasMatchingTags || titleContainsKeywords;
    }
    
    private List<String> suggestTags(String content) {
        // Analyse simple du contenu pour suggérer des tags - utilisez du NLP en production
        List<String> commonTags = tagRepository.findAll().stream()
            .map(Tag::getName)
            .collect(Collectors.toList());
            
        return commonTags.stream()
            .filter(tag -> content.toLowerCase().contains(tag.toLowerCase()))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private String suggestContentImprovements(String title, String partialContent, String category) {
        // Suggestion simple - utilisez une API NLP sophistiquée en production
        if (partialContent.length() < 100) {
            return "Votre article semble assez court. Essayez d'ajouter plus de détails pour enrichir le contenu.";
        } else if (!partialContent.contains(".")) {
            return "Pensez à structurer votre texte en phrases complètes pour une meilleure lisibilité.";
        } else {
            return "Votre article semble bien structuré. Pensez à ajouter des exemples concrets pour illustrer vos propos.";
        }
    }
    
    private List<Article> findSimilarArticlesByContent(String title, String content) {
        // Implémentation simple - extraire des mots-clés du titre et du contenu
        List<String> keywords = extractKeywords(title + " " + content);
        
        return articleRepository.findByPublishedTrue().stream()
            .filter(article -> isRelevantToKeywords(article, keywords))
            .limit(3)
            .collect(Collectors.toList());
    }
    
    private String analyzeReadability(String title, String content) {
        // Analyse simple de lisibilité - utilisez des algorithmes comme Flesch-Kincaid en production
        int avgWordsPerSentence = calculateAverageWordsPerSentence(content);
        
        if (avgWordsPerSentence > 25) {
            return "Vos phrases sont assez longues (moyenne de " + avgWordsPerSentence + " mots par phrase). Envisagez de les raccourcir pour améliorer la lisibilité.";
        } else if (avgWordsPerSentence < 10) {
            return "Vos phrases sont plutôt courtes (moyenne de " + avgWordsPerSentence + " mots par phrase). Vous pourriez enrichir votre contenu.";
        } else {
            return "La longueur de vos phrases est bien équilibrée (moyenne de " + avgWordsPerSentence + " mots par phrase).";
        }
    }
    
    private int calculateAverageWordsPerSentence(String content) {
        if (content == null || content.isEmpty()) return 0;
        
        String[] sentences = content.split("[.!?]");
        int totalSentences = sentences.length;
        
        if (totalSentences == 0) return 0;
        
        int totalWords = 0;
        for (String sentence : sentences) {
            String[] words = sentence.trim().split("\\s+");
            totalWords += words.length;
        }
        
        return totalWords / totalSentences;
    }
    
    private String suggestImprovements(Article article) {
        // Simplification - en production, utilisez NLP avancé
        StringBuilder suggestions = new StringBuilder();
        
        if (article.getTitle().length() > 80) {
            suggestions.append("Le titre est assez long, envisagez de le raccourcir pour plus d'impact.\n");
        }
        
        if (article.getTags() == null || article.getTags().size() < 3) {
            suggestions.append("Ajoutez plus de tags pour améliorer la visibilité de votre article.\n");
        }
        
        // Ajoutez d'autres suggestions pertinentes
        
        return suggestions.length() > 0 ? suggestions.toString() : "Votre article semble bien structuré !";
    }
    
    private List<String> suggestAdditionalTags(Article article) {
        // Récupérer le contenu de l'article
        String content = getArticleContent(article);
        
        // Extraire des mots-clés
        List<String> existingTags = article.getTags().stream()
            .map(Tag::getName)
            .collect(Collectors.toList());
        
        // Trouver des tags populaires qui pourraient être pertinents
        return tagRepository.findAll().stream()
            .map(Tag::getName)
            .filter(tagName -> !existingTags.contains(tagName))
            .filter(tagName -> content.toLowerCase().contains(tagName.toLowerCase()))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private String getArticleContent(Article article) {
        // Extraction du contenu textuel d'un article
        // Ceci est une méthode fictive, adaptez selon votre modèle de données
        return article.getTitle() + " " + (article.getRessources() != null ? 
               article.getRessources().stream()
                   .map(r -> r.getDescription())
                   .filter(Objects::nonNull)
                   .collect(Collectors.joining(" ")) : "");
    }
    
    /**
     * Get remaining API calls for today
     */
    public int getRemainingApiCalls() {
        String dateKey = LocalDate.now().toString();
        int currentCount = apiCallCounters.getOrDefault(dateKey, 0);
        return Math.max(0, dailyRateLimit - currentCount);
    }

    // Ajouter ces méthodes publiques pour le diagnostic
    
    /**
     * Teste si l'API du chatbot est accessible
     */
    public boolean testChatbotAvailability() {
        // Always return true since we're using local processing
        return true;
    }
    
    /**
     * Vérifie si une API key est configurée
     */
    public boolean isApiKeyConfigured() {
        return chatbotApiKey != null && !chatbotApiKey.trim().isEmpty() && 
               !chatbotApiKey.equals("your-default-key-for-development");
    }
    
    /**
     * Vérifie si le fallback est activé
     */
    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }
}
