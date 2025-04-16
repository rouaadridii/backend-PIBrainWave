package com.example.project2025.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private LocalDate date = LocalDate.now(); // Default to today's date

    @Size(max = 255, message = "L'URL de l'image ne doit pas dépasser 255 caractères")
    private String picture;

    private boolean status = false; // Default to false

    @Enumerated(EnumType.STRING)
    private PublicationStatus publicationStatus = PublicationStatus.DRAFT;

    @Min(value = 0, message = "Le nombre de vues ne peut pas être négatif")
    private Integer views;

    @Min(value = 0, message = "Le nombre de partages ne peut pas être négatif")
    private Integer numberShares;

    @NotNull(message = "La catégorie est obligatoire")
    @Enumerated(EnumType.STRING)
    private ArticleCategorie categorie;
    private LocalDateTime scheduledDate;
    private boolean published = false;
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference(value = "article-readlater")
    private Set<ReadLater> readLaters;

    public Set<Tag> getTags() {
        return tags;
    }
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getArticles().add(this);
    }

    // Remove a tag from the article and ensure bi-directional update
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getArticles().remove(this);
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    private boolean scheduled = false;  // Indique si l'article est programmé

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }


    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public boolean isPublished() {
        return published;
    }
    public PublicationStatus getPublicationStatus() {
        return publicationStatus;
    }

    public void setPublicationStatus(PublicationStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }


    @NotNull(message = "L'utilisateur est obligatoire")
    @ManyToOne
    @JsonBackReference(value = "user-articles") // Match the value in User
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "article")
    @JsonManagedReference(value = "article-ressources") // Specify a value

    private Set<Ressources> ressources = new HashSet<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference(value = "article-favorites") // Match the value in Favorite (if it exists)
    private Set<Favorite> favorites;

    // Constructeurs
    public Article() {
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getPicture() {
        return picture;
    }

    public Boolean getStatus() {
        return status;
    }

    public Integer getViews() {
        return views;
    }

    public Integer getNumberShares() {
        return numberShares;
    }

    public ArticleCategorie getCategorie() {
        return categorie;
    }

    public User getUser() {
        return user;
    }

    public Set<Ressources> getRessources() {
        return ressources;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public void setNumberShares(Integer numberShares) {
        this.numberShares = numberShares;
    }

    public void setCategorie(ArticleCategorie categorie) {
        this.categorie = categorie;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRessources(Set<Ressources> ressources) {
        this.ressources = ressources;
    }

    public void addRessource(Ressources ressource) {
        ressources.add(ressource);
        ressource.setArticle(this);
    }

    public void removeRessource(Ressources ressource) {
        ressources.remove(ressource);
        ressource.setArticle(null);
    }
}