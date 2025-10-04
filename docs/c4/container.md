# C4 — Уровень 2: Контейнеры

**Backend (Spring Boot)**
- API: REST (OpenAPI)
- Модули: Контент/Версии, Поиск, Безопасность, Хранилище
- Интеграции: PostgreSQL, OIDC

**Frontend**
- Получает контент/оглавление/поиск из Backend
- UI: левое оглавление, якоря секций, подсветка активной секции

**PostgreSQL**
- Таблицы: article, chapter, section, tag, article_tag, revision, media_asset, user, role, user_role
- Индексы: GIN по tsvector для поиска

**OIDC Provider**
- Выдает access token (Bearer) для ADMIN
