package com.example.instructions.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Конвертер ролей из JWT в authorities Spring Security.
 */
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        if (jwt.hasClaim(ROLES)) {
            roles.addAll(jwt.getClaimAsStringList(ROLES));
        }
        if (jwt.hasClaim(REALM_ACCESS)) {
            Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);
            Object rawRoles = realmAccess.get(ROLES);
            if (rawRoles instanceof Collection<?> collection) {
                for (Object role : collection) {
                    roles.add(String.valueOf(role));
                }
            }
        }
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
