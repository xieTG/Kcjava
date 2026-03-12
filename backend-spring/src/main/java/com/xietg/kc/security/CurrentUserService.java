package com.xietg.kc.security;

import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
        }

        String email = authentication.getName().trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }
}