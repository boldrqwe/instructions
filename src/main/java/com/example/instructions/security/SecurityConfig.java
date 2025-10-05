package com.example.instructions.security;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * Конфигурация безопасности: OAuth2 resource server + RBAC.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(AdminAccountProperties.class)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()   // ✅ открыть логин
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles", "/api/v1/articles/*", "/api/v1/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles/*/toc").permitAll()
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/logout").permitAll() // опционально
                        .anyRequest().hasRole("ADMIN")
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .httpBasic(Customizer.withDefaults());

        // 🔹 добавить return
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder,
                                                                 AdminAccountProperties adminAccountProperties) {
        String username = StringUtils.hasText(adminAccountProperties.username())
                ? adminAccountProperties.username()
                : AdminAccountProperties.DEFAULT_USERNAME;
        String password = StringUtils.hasText(adminAccountProperties.password())
                ? adminAccountProperties.password()
                : UUID.randomUUID().toString();

        log.info("Admin credentials: username='{}', password='{}'", username, password);

        UserDetails admin = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
