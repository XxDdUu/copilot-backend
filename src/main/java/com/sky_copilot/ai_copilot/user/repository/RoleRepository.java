package com.sky_copilot.ai_copilot.user.repository;

import com.sky_copilot.ai_copilot.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository
        extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}