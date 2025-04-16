package com.example.project2025.Repositories;

import com.example.project2025.Entities.Favorite;
import com.example.project2025.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);

    Optional<Favorite> findByUser_IdAndArticle_Id(Long userId, Long articleId);
}