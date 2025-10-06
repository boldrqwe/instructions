package com.example.instructions.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class JwtTokenServiceTest {

    @Test
    void generateTokenWithShortSecretSucceeds() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("short");
        SecurityConfig securityConfig = new SecurityConfig();
        JwtTokenService service = new JwtTokenService(securityConfig.jwtEncoder(properties), properties);
        Authentication authentication = new TestingAuthenticationToken("user", null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        JwtTokenService.TokenResponse token = service.generateToken(authentication);

        assertThat(token.token()).isNotBlank();
        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresAt()).isNotNull();
    }
}
