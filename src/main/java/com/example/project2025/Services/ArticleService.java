package com.example.project2025.Services;

import com.example.project2025.Entities.*;
import com.example.project2025.Repositories.ArticleReactionRepository;
import com.example.project2025.Repositories.ArticleRepository;
import com.example.project2025.Repositories.TagRepository;
import com.example.project2025.Repositories.UserRepository;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleReactionRepository articleReactionRepository;
    @Autowired
    private TagRepository tagRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    @Transactional
    public Article createArticle(Article article, Long userId,
                                 MultipartFile pictureFile, MultipartFile videoFile, MultipartFile pdfFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + userId));
        article.setUser(user);

        if (article.getPublicationStatus() == null) {
            article.setPublicationStatus(PublicationStatus.DRAFT); // Default to DRAFT
        }

        if (article.getPublicationStatus() == PublicationStatus.DRAFT) {
            article.setPublished(true); // Drafts are immediately considered published
        } else if (article.getPublicationStatus() == PublicationStatus.PUBLISHED) {
            article.setPublicationStatus(PublicationStatus.PENDING_APPROVAL); // Requires admin approval
            article.setPublished(false); // Not yet fully published
        } else {
            article.setPublished(false); // Default to not published for other statuses
        }

        // Handle main picture
        if (pictureFile != null && !pictureFile.isEmpty()) {
            article.setPicture(saveFile(pictureFile));
        }

        // Handle video as a resource
        if (videoFile != null && !videoFile.isEmpty()) {
            Ressources videoResource = new Ressources();
            videoResource.setVideo(saveFile(videoFile));
            videoResource.setDescription("Video"); // You can set a more descriptive name
            article.addRessource(videoResource);
        }

        // Handle pdf as a resource
        if (pdfFile != null && !pdfFile.isEmpty()) {
            Ressources pdfResource = new Ressources();
            pdfResource.setPdf(saveFile(pdfFile));
            pdfResource.setDescription("PDF"); // You can set a more descriptive name
            article.addRessource(pdfResource);
        }

        // Handle Tags
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (Tag tag : article.getTags()) {
                Optional<Tag> existingTagOptional = tagRepository.findByName(tag.getName());
                if (existingTagOptional.isPresent()) {
                    tags.add(existingTagOptional.get()); // Reuse existing tag
                } else {
                    tags.add(tagRepository.save(tag)); // Save new tag
                }
            }
            article.setTags(tags);
        }

        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticle(Long id, Article article,
                                 MultipartFile pictureFile, MultipartFile videoFile, MultipartFile pdfFile) throws IOException {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + id));
        User user = userRepository.findById(article.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + article.getUser().getId()));

        existingArticle.setTitle(article.getTitle());
        existingArticle.setDate(article.getDate());
        existingArticle.setPicture(article.getPicture()); // Keep existing if no new picture

        // Handle video resource update (you might need more complex logic to update existing)
        if (videoFile != null && !videoFile.isEmpty()) {
            Ressources videoResource = new Ressources();
            videoResource.setVideo(saveFile(videoFile));
            videoResource.setDescription("Updated Video");
            existingArticle.addRessource(videoResource);
        }

        // Handle pdf resource update (you might need more complex logic to update existing)
        if (pdfFile != null && !pdfFile.isEmpty()) {
            Ressources pdfResource = new Ressources();
            pdfResource.setPdf(saveFile(pdfFile));
            pdfResource.setDescription("Updated PDF");
            existingArticle.addRessource(pdfResource);
        }

        // Handle tags update
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            Set<Tag> updatedTags = new HashSet<>();
            for (Tag tag : article.getTags()) {
                Optional<Tag> existingTagOptional = tagRepository.findByName(tag.getName());
                if (existingTagOptional.isPresent()) {
                    updatedTags.add(existingTagOptional.get());
                } else {
                    updatedTags.add(tagRepository.save(tag));
                }
            }
            existingArticle.setTags(updatedTags);
        }

        return articleRepository.save(existingArticle);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);
        return fileName;
    }

    @Transactional
    public Article transitionArticleStatus(Long id, PublicationStatus newStatus) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + id));

        article.setPublicationStatus(newStatus);
        return articleRepository.save(article);
    }

    public List<Article> getArticlesByStatus(PublicationStatus status) {
        return articleRepository.findByPublicationStatus(status);
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    public Optional<Article> incrementView(Long id) {
        return articleRepository.findById(id).map(article -> {
            article.setViews(article.getViews() == null ? 1 : article.getViews() + 1);
            return articleRepository.save(article);
        });
    }

    public List<Article> getTop5MostViewedArticles() {
        return articleRepository.findTop5ByOrderByViewsDesc();
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void publishScheduledArticles() {
        List<Article> scheduledArticles = articleRepository.findByScheduledDateBeforeAndPublishedFalse(LocalDateTime.now());
        for (Article article : scheduledArticles) {
            // Vérifier le statut avant de publier via la planification
            if (article.getPublicationStatus() == PublicationStatus.PENDING_REVIEW) {
                article.setPublished(true);
                article.setPublicationStatus(PublicationStatus.PUBLISHED);
                articleRepository.save(article);
            }
        }
    }


    @Transactional
    public Article approveArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + id));

        if (article.getPublicationStatus() == PublicationStatus.PENDING_APPROVAL) {
            article.setPublished(true);
            article.setPublicationStatus(PublicationStatus.PUBLISHED);
            return articleRepository.save(article);
        } else {
            // Optionally handle cases where the article is not in PENDING_APPROVAL state
            // You could throw an exception or log a warning.
            return article; // Or throw new IllegalStateException("Article is not pending approval.");
        }
    }

    public Article getArticleWithRessources(Long id) {
        return articleRepository.findArticleWithRessources(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + id));
    }
    @Transactional
    public void addOrUpdateReaction(Long articleId, Long userId, String reactionType) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + userId));

        if (!isValidReactionType(reactionType)) {
            throw new IllegalArgumentException("Type de réaction invalide: " + reactionType);
        }

        Optional<ArticleReaction> existingReaction = articleReactionRepository.findByArticleAndUser(article, user);
        if (existingReaction.isPresent()) {
            existingReaction.get().setReactionType(reactionType);
        } else {
            ArticleReaction reaction = new ArticleReaction();
            reaction.setArticle(article);
            reaction.setUser(user);
            reaction.setReactionType(reactionType);
            articleReactionRepository.save(reaction);
        }
    }

    @Transactional
    public void removeReaction(Long articleId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + userId));

        articleReactionRepository.findByArticleAndUser(article, user)
                .ifPresent(articleReactionRepository::delete);
    }

    public Map<String, Long> getReactionCounts(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + articleId));
        List<Object[]> results = articleReactionRepository.countReactionsByArticle(article);
        Map<String, Long> reactionCounts = new HashMap<>();
        for (Object[] result : results) {
            reactionCounts.put((String) result[0], (Long) result[1]);
        }
        return reactionCounts;
    }

    public String getUserReaction(Long articleId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + userId));
        Optional<ArticleReaction> reaction = articleReactionRepository.findByArticleAndUser(article, user);
        return reaction.map(ArticleReaction::getReactionType).orElse(null);
    }

    private boolean isValidReactionType(String type) {
        return List.of("like", "love", "haha", "wow", "sad", "angry").contains(type);
    }

    /**
     * Find similar articles based on common tags
     * @param articleId The ID of the article to find similar articles for
     * @param limit Maximum number of similar articles to return
     * @return List of similar articles ordered by similarity score (number of common tags)
     */
    public List<Article> findSimilarArticles(Long articleId, int limit) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        Set<Tag> targetArticleTags = article.getTags();

        if (targetArticleTags == null || targetArticleTags.isEmpty()) {
            return List.of(); // No tags to find similar articles
        }

        // Create a map to store similarity scores
        Map<Article, Integer> similarityScores = new HashMap<>();

        // Find all articles with at least one common tag
        List<Article> articles = articleRepository.findAll().stream()
                .filter(a -> !a.getId().equals(articleId))  // Exclude current article
                .filter(a -> a.getTags() != null && !a.getTags().isEmpty())
                .collect(Collectors.toList());

        // Calculate similarity scores (number of matching tags)
        for (Article otherArticle : articles) {
            int commonTags = 0;
            for (Tag tag : otherArticle.getTags()) {
                if (targetArticleTags.contains(tag)) {
                    commonTags++;
                }
            }

            if (commonTags > 0) {
                similarityScores.put(otherArticle, commonTags);
            }
        }

        // Sort by similarity score and limit results
        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Article, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Add tags to an article
     * @param articleId The article ID
     * @param tagNames List of tag names to add
     * @return The updated article
     */
    @Transactional
    public Article addTagsToArticle(Long articleId, List<String> tagNames) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        for (String tagName : tagNames) {
            // Find or create the tag
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag(tagName);
                        return tagRepository.save(newTag);
                    });

            article.addTag(tag);
        }

        return articleRepository.save(article);
    }

    /**
     * Remove tags from an article
     * @param articleId The article ID
     * @param tagNames List of tag names to remove
     * @return The updated article
     */
    @Transactional
    public Article removeTagsFromArticle(Long articleId, List<String> tagNames) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        for (String tagName : tagNames) {
            tagRepository.findByName(tagName).ifPresent(article::removeTag);
        }

        return articleRepository.save(article);
    }

    /**
     * Remove a tag from an article
     * @param articleId The article ID
     * @param tagId The tag ID to remove
     * @return The updated article
     */
    @Transactional
    public Article removeTagFromArticle(Long articleId, Long tagId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        article.removeTag(tag);
        return articleRepository.save(article);
    }

    /**
     * Get all available tags
     * @return List of all tags
     */
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}

