package com.example.project2025.Repositories;

import com.example.project2025.Entities.Article;
import com.example.project2025.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long> {
}
