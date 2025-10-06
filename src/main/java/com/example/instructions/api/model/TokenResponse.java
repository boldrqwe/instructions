package com.example.instructions.api.model;


/**
 * Ответ при успешной авторизации.
 */
public record TokenResponse(
        String token,
        String tokenType,
        String expiresAt
) {}
