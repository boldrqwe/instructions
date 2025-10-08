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

## Запуск локально
1. Поднимите PostgreSQL 15 (пример через Docker):
   ```bash
   docker run --rm -p 5432:5432 -e POSTGRES_DB=instructions -e POSTGRES_USER=instructions -e POSTGRES_PASSWORD=instructions postgres:15-alpine
   ```
2. Примените миграции и соберите проект:
   ```bash
   ./mvnw clean test
   ```
3. Запустите приложение:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Swagger UI доступен по адресу `http://localhost:8080/swagger-ui.html`.

### Полезные команды
- `./mvnw test` — юнит и интеграционные тесты (PostgreSQL через Testcontainers).
- `./mvnw spring-boot:run` — запуск приложения.
- `./mvnw -DskipTests package` — сборка jar без тестов.

Любые изменения контракта API делаются **только** через ADR и правку `/docs/api/openapi.yaml`.

## Article API & Uploads

- **Админ-эндпоинты (`/api/v1/articles`)**: CRUD черновиков, публикация/снятие с публикации, фильтрация по статусу и полнотекстовому запросу (`status`, `query`, `page`, `size`). Все операции требуют роли `ADMIN`.
- **Публичный просмотр**: `GET /api/v1/articles/by-slug/{slug}` возвращает только опубликованные статьи с полями summary, контентом в HTML/JSON и тегами.
- **Загрузка изображений**: `POST /api/v1/uploads/images` принимает PNG/JPEG/WebP/GIF размером до 10 МБ, сохраняет в `./uploads/images/YYYY/MM/DD/{UUID}.{ext}` и возвращает URL вида `/uploads/...`.
- **Статика**: файлы из каталога `./uploads` автоматически раздаются по `/uploads/**`.
- **Dev CORS**: при профиле `dev` разрешён origin `http://localhost:5173` для `/api/**` и `/uploads/**`.
