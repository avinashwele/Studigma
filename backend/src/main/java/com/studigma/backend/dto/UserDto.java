package com.studigma.backend.dto;

import java.util.UUID;

import com.studigma.backend.enums.AuthProvider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String email;
    private String name;
    private String profileImageUrl;
    private AuthProvider provider;
}