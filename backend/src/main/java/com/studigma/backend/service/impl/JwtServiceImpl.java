package com.studigma.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.studigma.backend.config.JwtProperties;
import com.studigma.backend.entity.User;
import com.studigma.backend.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    // 🔐 Signing Key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    // ==============================
    // ✅ ACCESS TOKEN
    // ==============================
    @Override
    public String generateAccessToken(User user) {
        return buildAccessToken(user);
    }

    // ==============================
    // ✅ REFRESH TOKEN
    // ==============================
    @Override
    public String generateRefreshToken(User user) {
        return buildRefreshToken(user);
    }

    // 🔧 Access Token Builder
    private String buildAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // subject = email
                .claim("userId", user.getId().toString())
                .claim("provider", user.getProvider().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()
                        + jwtProperties.getAccessExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔧 Refresh Token Builder
    private String buildRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()
                        + jwtProperties.getRefreshExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ==============================
    // ✅ Validate Token
    // ==============================
    @Override
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ==============================
    // ✅ Extract Email
    // ==============================
    @Override
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 🔍 Extract Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
