package com.example.instructions.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки административного аккаунта, используемого для HTTP Basic авторизации.
 */
@ConfigurationProperties(prefix = "app.admin")
public record AdminAccountProperties(String username, String password) {

    public static final String DEFAULT_USERNAME = "admin";

    public AdminAccountProperties {
        // record canonical constructor for future validation hooks
    }
}
