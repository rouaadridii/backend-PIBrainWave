package com.example.project2025.Repositories;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.ArticleReaction;
import com.example.project2025.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ArticleReactionRepository extends JpaRepository<ArticleReaction, Long> {
    Optional<ArticleReaction> findByArticleAndUser(Article article, User user);
    List<ArticleReaction> findByArticle(Article article);
    List<ArticleReaction> findByArticleAndReactionType(Article article, String reactionType);

    @Query("SELECT COUNT(ar) FROM ArticleReaction ar WHERE ar.article = :article AND ar.reactionType = :reactionType")
    long countByArticleAndReactionType(@Param("article") Article article, @Param("reactionType") String reactionType);

    @Query("SELECT ar.reactionType, COUNT(ar) FROM ArticleReaction ar WHERE ar.article = :article GROUP BY ar.reactionType")
    List<Object[]> countReactionsByArticle(@Param("article") Article article);
}