# Безопасность (OIDC + RBAC)

## Аутентификация
- Resource Server: Bearer JWT, заголовок `Authorization: Bearer <token>`.
- Публичные GET (чтение) — без токена.

## Роли
- ADMIN — доступ к мутирующим эндпоинтам.
- READER — роль для будущего расширения (например, приватные черновики для редакторов).

## Авторизация
- Конфиг уровня маршрутов (Spring Security): `/api/v1/**`:
    - `GET /articles`, `GET /articles/{slug}`, `GET /articles/{id}/toc`, `GET /search` — permitAll
    - Остальное — `hasRole('ADMIN')`

## Инпут-валидация и лимиты
- Размеры полей и multipart ограничены.
- Очистка Markdown на фронте; сервер — хранит как текст, но API не отдаёт небезопасный HTML.

## Логи/аудит
- Успешные публикации логируются с `articleId` и `version`.
- Ошибки 4xx/5xx — структурированный JSON (code, message).
