# ADR-002: Аутентификация OIDC, роли ADMIN/READER

- **Решение:** Spring Security Resource Server, Bearer JWT. Роли: ADMIN, READER.
- **Почему:** стандартная интеграция, централизованный IAM (Keycloak и т.п.).
- **Правила:** публичные GET открыты, мутирующие операции — только ADMIN.
