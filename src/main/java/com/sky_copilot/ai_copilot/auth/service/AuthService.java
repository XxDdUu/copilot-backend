package com.sky_copilot.ai_copilot.auth.service;

import com.sky_copilot.ai_copilot.auth.dto.LoginRequest;
import com.sky_copilot.ai_copilot.auth.dto.LoginResponse;
import com.sky_copilot.ai_copilot.auth.dto.RegisterRequest;
import com.sky_copilot.ai_copilot.auth.dto.RegisterResponse;
import com.sky_copilot.ai_copilot.auth.security.JwtTokenProvider;
import com.sky_copilot.ai_copilot.user.entity.Role;
import com.sky_copilot.ai_copilot.user.entity.User;
import com.sky_copilot.ai_copilot.user.repository.RoleRepository;
import com.sky_copilot.ai_copilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_USER not found — run migrations first"
                ));
                

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);
        
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                token
        );
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "User not found after authentication"
                ));

        String token = jwtTokenProvider.generateToken(user.getEmail());

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                token,
                roles
        );
    }
}