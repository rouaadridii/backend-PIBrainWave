package com.example.project2025.Repositories;

import com.example.project2025.Entities.ReadLater;
import com.example.project2025.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReadLaterRepository extends JpaRepository<ReadLater, Long> {
    List<ReadLater> findByUser(User user);
    
    Optional<ReadLater> findByUser_IdAndArticle_Id(Long userId, Long articleId);
    


    @Query("SELECT rl FROM ReadLater rl WHERE rl.notified = false AND rl.reminderDate <= :nowUtc")
    List<ReadLater> findPendingReminders(@Param("nowUtc") LocalDateTime nowUtc);
}
