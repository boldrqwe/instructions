package com.example.instructions.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Хелпер для извлечения текущего пользователя из контекста безопасности.
 */
@Component
public class AuthenticationFacade {

    /**
     * Возвращает идентификатор текущего пользователя или "system" если пользователь отсутствует.
     *
     * @return идентификатор пользователя
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "system";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getSubject()).orElse("system");
        }
        return Optional.ofNullable(authentication.getName()).orElse("system");
    }
}
