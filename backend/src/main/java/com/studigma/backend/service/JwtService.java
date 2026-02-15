package com.studigma.backend.service;

import com.studigma.backend.entity.User;

public interface JwtService {

    // Generate Access Token
    String generateAccessToken(User user);

    // Generate Refresh Token
    String generateRefreshToken(User user);

    // Validate Token
    boolean isTokenValid(String token);

    // Extract Email (used for authentication)
    String extractEmail(String token);
}
