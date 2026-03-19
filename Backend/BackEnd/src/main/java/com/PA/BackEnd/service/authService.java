package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.authResponse;
import com.PA.BackEnd.dto.loginRequest;
import com.PA.BackEnd.dto.registerRequest;
import com.PA.BackEnd.model.appUser;
import com.PA.BackEnd.repository.appUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class authService {
    private final appUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final jwtService jwtService;

    public authService(appUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       jwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public authResponse register(registerRequest request) {
        String email = normalizeEmail(request.getEmail());
        String password = safeTrim(request.getPassword());
        validatePassword(password);

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        appUser user = new appUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(Instant.now());
        appUserRepository.save(user);

        return new authResponse(jwtService.generateToken(email), email);
    }

    public authResponse login(loginRequest request) {
        String email = normalizeEmail(request.getEmail());
        String password = safeTrim(request.getPassword());

        appUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return new authResponse(jwtService.generateToken(email), email);
    }

    private String normalizeEmail(String email) {
        String normalized = safeTrim(email).toLowerCase();
        if (!normalized.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        return normalized;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
