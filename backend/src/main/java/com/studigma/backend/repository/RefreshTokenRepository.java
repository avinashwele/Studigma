package com.studigma.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.studigma.backend.entity.RefreshToken;
import com.studigma.backend.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	
Optional<RefreshToken> findByToken(String token);
    
    void deleteByUser(User user);
	

}
