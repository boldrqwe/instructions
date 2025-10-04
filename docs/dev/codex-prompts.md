# Шаблоны промптов для Codex

## Backend: публикация статьи
Роль: Senior Java/Spring Boot backend engineer.
Контекст: см. `/docs/vision.md`, `/docs/adr/*.md`, `/docs/api/openapi.yaml`, `/docs/backend/*.md`.
Задача: Реализовать endpoint `POST /api/v1/articles/{id}:publish` по OpenAPI.
Требования:
- Только ADMIN (см. security.md).
- Проверить статус `DRAFT`, иначе 409.
- Инкремент `version`, записать `Revision` (snapshot), обновить индексы поиска.
- Транзакция.
- Тесты: JUnit5 + Testcontainers (happy + 403/404/409).
- Логи JSON (info) с `articleId`, `version`.
  Вывод Codex: список файлов, полный diff, объяснение шагов, команда сборки.

## Backend: поиск
Роль: Senior Java/Spring Boot backend engineer.
Задача: `GET /api/v1/search?query=...`
Требования:
- Поиск только в PUBLISHED.
- Источники: `article.title`, `section.title + markdown` (tsvector).
- Пагинация, сниппет из markdown (обрезка безопасно).
- Тесты: интеграционные с фикстурами.

## Frontend: страница чтения
Роль: Senior Frontend Engineer (React/Vite или Next.js).
Контекст: `/docs/frontend/*.md`, `/docs/api/openapi.yaml`.
Задача: `/articles/{slug}` — контент + левый TOC.
Требования:
- Получить article и toc, отрисовать дерево, скролл к секции, активная подсветка.
- Рендер Markdown (sanitize), поддержка кода/таблиц.
- 404 и лоадинг-состояния.
- Тесты на TOC-навигацию и генерацию якорей.
