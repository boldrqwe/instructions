package com.example.instructions.api;

import com.example.instructions.common.ForbiddenException;
import com.example.instructions.security.JwtTokenService;
import com.example.instructions.security.JwtTokenService.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Требуется базовая аутентификация");
        }

        TokenResponse response = jwtTokenService.generateToken(authentication);
        return ResponseEntity.ok(response);
    }
}
