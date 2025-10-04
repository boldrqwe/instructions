# Instructions — Backend

Сервис для публикации «больших инструкций» (почти как книги): админ создает и редактирует статьи, главы и параграфы; пользователи читают опубликованный контент. Публичная часть напоминает структуру страниц Netdata Academy (левое оглавление, удобная навигация), но без копирования дизайна.

## Быстрые ссылки
- Видение/цели: [/docs/vision.md](docs/vision.md)
- Модели/схема: [/docs/backend/domain-model.md](docs/backend/domain-model.md), [/docs/backend/persistence.md](docs/backend/persistence.md)
- Безопасность (OIDC/RBAC): [/docs/backend/security.md](docs/backend/security.md)
- Контракт API (истина): [/docs/api/openapi.yaml](docs/api/openapi.yaml)
- Решения (ADR): [/docs/adr](docs/adr)
- Правила для Codex/PR: [/docs/dev/codex-rules.md](docs/dev/codex-rules.md), [/docs/dev/codex-prompts.md](docs/dev/codex-prompts.md)
- UX фронта (референс внешний): [/docs/frontend/ux.md](docs/frontend/ux.md)

## Технологии
- Java 17+, Spring Boot, Spring Security (OIDC Resource Server)
- PostgreSQL 15+ (Flyway миграции, tsvector поиск)
- Testcontainers для интеграционных тестов
- OpenAPI (springdoc)
- Логи JSON, метрики/трейсинг (OTel) — интеграция на следующих итерациях

## Запуск локально (черновик)
- `./mvnw spring-boot:run` (нужна локальная Postgres) или с Testcontainers профилем.
- См. `application.yml` и `/docs/backend/persistence.md`.

Любые изменения контракта API делаются **только** через ADR и правку `/docs/api/openapi.yaml`.
