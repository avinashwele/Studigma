package com.studigma.backend.service;

import com.studigma.backend.dto.AuthResponse;
import com.studigma.backend.dto.LoginRequest;
import com.studigma.backend.dto.RegisterRequest;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(String idToken) throws Exception;

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
