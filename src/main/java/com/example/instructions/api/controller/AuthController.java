package com.example.instructions.api.controller;

import com.example.instructions.api.model.AuthProfileResponse;
import com.example.instructions.security.JwtTokenService;
import com.example.instructions.security.JwtTokenService.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер аутентификации.
 * /api/v1/auth/login — выдаёт JWT-токен по Basic auth.
 * /api/v1/auth/profile — проверяет текущего пользователя (по Bearer токену).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Basic Auth → выдаёт JWT токен.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Неуспешная попытка входа (authentication == null)");
            return ResponseEntity.status(401).build();
        }

        log.info("Вход пользователя: username='{}', roles={}",
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );

        TokenResponse response = jwtTokenService.generateToken(authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Bearer JWT → возвращает профиль текущего пользователя.
     */
    @GetMapping("/profile")
    public ResponseEntity<AuthProfileResponse> profile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Неавторизованный запрос к /profile");
            return ResponseEntity.status(401).build();
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        AuthProfileResponse response = new AuthProfileResponse(true, authentication.getName(), roles);

        log.info("Профиль пользователя: username='{}', roles={}", response.username(), response.roles());

        return ResponseEntity.ok(response);
    }
}
