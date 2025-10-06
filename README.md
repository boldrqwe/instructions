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

### REST API

#### Статьи

- `POST /api/v1/articles` — создание черновика (ROLE_ADMIN). Пример:

  ```bash
  curl -X POST http://localhost:8080/api/v1/articles \
    -H "Authorization: Bearer <token>" \
    -H "Content-Type: application/json" \
    -d '{
      "title": "Новый гид",
      "summary": "Краткое описание",
      "contentHtml": "<p>HTML контент</p>",
      "contentJson": {"blocks": []},
      "tags": ["postgres", "spring"]
    }'
  ```

- `PUT /api/v1/articles/{id}` — редактирование черновика (ROLE_ADMIN).
- `POST /api/v1/articles/{id}/publish` — публикация (ROLE_ADMIN).
- `POST /api/v1/articles/{id}/unpublish` — возврат в черновик (ROLE_ADMIN).
- `GET /api/v1/articles?status=&query=&page=&size=&authorId=` — поиск и пагинация. Без токена возвращаются только опубликованные статьи.
- `GET /api/v1/articles/{id}` — просмотр статьи по ID (ROLE_ADMIN).
- `GET /api/v1/articles/by-slug/{slug}` — публичный просмотр опубликованной статьи.

#### Загрузка изображений

- `POST /api/v1/uploads/images` — загрузка файла (ROLE_ADMIN, rate limit 20/min на пользователя). Файл сохраняется в `./uploads/images/YYYY/MM/DD/<uuid>.<ext>`, URL ответа можно отдавать фронтенду.

  ```bash
  curl -X POST http://localhost:8080/api/v1/uploads/images \
    -H "Authorization: Bearer <token>" \
    -F "file=@cover.png"
  ```

Статика из каталога `./uploads` раздается по пути `/uploads/**`.
