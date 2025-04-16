package com.example.project2025.Entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Article> articles = new HashSet<>();

    // Default constructor
    public Tag() {
    }

    // Constructor with name
    @JsonCreator
    public Tag(String name) {
        this.name = name;
    }

    // JsonValue to serialize tag as a string
    @JsonValue
    public String getName() {
        return name;
    }

    // Equals and hashCode based on name
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return name != null && name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    // Static factory method for easier creation from string
    public static Tag fromString(String name) {
        return new Tag(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    public Tag(String name, Set<Article> articles) {
        this.name = name;
        this.articles = articles;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
