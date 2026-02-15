package com.studigma.backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.studigma.backend.dto.AuthResponse;
import com.studigma.backend.dto.LoginRequest;
import com.studigma.backend.dto.RegisterRequest;
import com.studigma.backend.entity.RefreshToken;
import com.studigma.backend.entity.User;
import com.studigma.backend.enums.AuthProvider;
import com.studigma.backend.mapper.UserMapper;
import com.studigma.backend.repository.RefreshTokenRepository;
import com.studigma.backend.repository.UserRepository;
import com.studigma.backend.service.JwtService;
import com.studigma.backend.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id}")
    private String googleClientId;

    // ==================================
    // ✅ REGISTER
    // ==================================
    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("Email already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = UserMapper.toLocalUser(request, encodedPassword);

        // Optional avatar
        String avatarUrl = "https://ui-avatars.com/api/?name="
                + request.getName().replace(" ", "+");
        user.setProfileImageUrl(avatarUrl);

        userRepository.save(user);

        return generateTokens(user);
    }

    // ==================================
    // ✅ LOGIN (LOCAL)
    // ==================================
    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new BadCredentialsException("Please login using Google");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return generateTokens(user);
    }

    // ==================================
    // ✅ GOOGLE LOGIN
    // ==================================
    @Override
    public AuthResponse loginWithGoogle(String idToken) throws Exception {

        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        new GsonFactory())
                        .setAudience(List.of(googleClientId))
                        .build();

        GoogleIdToken token = verifier.verify(idToken);

        if (token == null) {
            throw new BadCredentialsException("Invalid Google token");
        }

        GoogleIdToken.Payload payload = token.getPayload();

        if (!payload.getEmailVerified()) {
            throw new BadCredentialsException("Email not verified");
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = UserMapper.toGoogleUser(
                            email,
                            name,
                            googleId,
                            picture
                    );
                    return userRepository.save(newUser);
                });

        return generateTokens(user);
    }

    // ==================================
    // 🔐 GENERATE TOKENS
    // ==================================
    private AuthResponse generateTokens(User user) {

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Remove old refresh tokens
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(
                accessToken,
                refreshToken,
                UserMapper.toDto(user)
        );
    }

    // ==================================
    // 🔁 REFRESH TOKEN
    // ==================================
    @Override
    public AuthResponse refreshToken(String refreshToken) {

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new BadCredentialsException("Refresh token expired");
        }

        User user = storedToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);

        return new AuthResponse(
                newAccessToken,
                refreshToken,
                UserMapper.toDto(user)
        );
    }

    // ==================================
    // 🚪 LOGOUT
    // ==================================
    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}
