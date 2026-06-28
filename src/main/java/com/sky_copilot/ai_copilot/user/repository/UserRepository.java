package com.sky_copilot.ai_copilot.user.repository;

import com.sky_copilot.ai_copilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}