package com.example.project2025.Repositories;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.ArticleCategorie;
import com.example.project2025.Entities.PublicationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.ressources WHERE a.id = :id")
    Optional<Article> findArticleWithRessources(@Param("id") Long id);
    List<Article> findTop5ByOrderByViewsDesc();
    List<Article> findByScheduledDateBeforeAndPublishedFalse(LocalDateTime now);
    List<Article> findByPublishedTrueOrScheduledDateBeforeAndScheduledTrue(LocalDateTime now);
    List<Article> findByPublishedTrue();

    List<Article> findByPublicationStatus(PublicationStatus status);
    List<Article> findByPublishedTrue(Sort sort);
    List<Article> findByPublishedTrueAndCategorie(ArticleCategorie categorie);

}