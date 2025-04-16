package com.example.project2025.Services;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.Favorite;
import com.example.project2025.Entities.User;
import com.example.project2025.Repositories.FavoriteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    public Favorite addFavorite(User user, Article article) {
        List<Favorite> existingFavorites = favoriteRepository.findByUser(user);
        for (Favorite favorite : existingFavorites) {
            if (favorite.getArticle().equals(article)) {
                throw new RuntimeException("Article déjà dans les favoris.");
            }
        }
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setArticle(article);
        return favoriteRepository.save(favorite);
    }


    public void removeFavorite(Long id) {
        favoriteRepository.deleteById(id);
    }


    public List<Favorite> getFavoritesByUser(User user) {
        return favoriteRepository.findByUser(user);
    }
} 