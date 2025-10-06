package com.example.instructions.api.model;

import java.util.List;

/**
 * Ответ на запрос профиля текущего пользователя.
 */
public record AuthProfileResponse(
        boolean authenticated,
        String username,
        List<String> roles
) {}
