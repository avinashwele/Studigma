package com.studigma.backend.mapper;

import com.studigma.backend.dto.RegisterRequest;
import com.studigma.backend.dto.UserDto;
import com.studigma.backend.entity.User;
import com.studigma.backend.enums.AuthProvider;

public class UserMapper {

    private UserMapper() {}

    // Entity → DTO
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                user.getProvider()
        );
    }

    // RegisterRequest → Entity (LOCAL)
    public static User toLocalUser(RegisterRequest request, String encodedPassword) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .provider(AuthProvider.LOCAL)
                .build();
    }

    // Google User Creation
    public static User toGoogleUser(
            String email,
            String name,
            String googleId,
            String profileImageUrl
    ) {
        return User.builder()
                .email(email)
                .name(name)
                .googleId(googleId)
                .profileImageUrl(profileImageUrl)
                .provider(AuthProvider.GOOGLE)
                .build();
    }
}
