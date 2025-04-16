package com.example.project2025.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }



    @ManyToOne
    @JsonBackReference(value = "user-favorites") // Match the value in User
    private User user;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    @JsonBackReference(value = "article-favorites") // Match the value in Article
    private Article article;
}