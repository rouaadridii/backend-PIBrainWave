package com.example.project2025.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "user_id"})
)
public class ArticleReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public LocalDateTime getReactionTimestamp() {
        return reactionTimestamp;
    }

    public void setReactionTimestamp(LocalDateTime reactionTimestamp) {
        this.reactionTimestamp = reactionTimestamp;
    }

    @Column(nullable = false)
    private String reactionType; // "like", "love", "haha", "wow", "sad", "angry"

    @Column(name = "reaction_timestamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime reactionTimestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleReaction)) return false;
        ArticleReaction that = (ArticleReaction) o;
        return Objects.equals(article, that.article) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(article, user);
    }
}