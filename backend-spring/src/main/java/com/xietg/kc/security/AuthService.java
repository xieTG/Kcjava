package com.xietg.kc.security;

import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public UserEntity requireUser(String authorizationHeader) {
        String token = bearerToken(authorizationHeader);
        if (token == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing token");
        }

        JwtService.JwtPayload payload = jwtService.decodeToken(token);
        if (payload.subject() == null || payload.subject().isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        return userRepository.findByEmail(payload.subject().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    private String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String h = authorizationHeader.trim();
        if (!h.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        return h.substring("bearer ".length()).trim();
    }
}
