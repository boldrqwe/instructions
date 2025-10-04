# Persistence (PostgreSQL + Flyway)

## Таблицы (MVP)
- `article (id uuid pk, title, slug, status, version int, created_by, created_at, updated_at)`
- `chapter (id uuid pk, article_id fk, title, order_index int)`
- `section (id uuid pk, chapter_id fk, title, order_index int, markdown text)`
- `tag (id uuid pk, name unique, slug unique)`
- `article_tag (article_id fk, tag_id fk, pk(article_id, tag_id))`
- `revision (id uuid pk, article_id fk, version int, snapshot jsonb, created_at)`
- `media_asset (id uuid pk, url text, mime, size bigint, owner_id, created_at)`

## Поиск
- Материализуем tsvector поля:
    - `article.search_vector` по title
    - `section.search_vector` по title + markdown
- GIN индексы на оба поля.
- Заполняем/обновляем в триггерах/сервисе.

## Flyway
- Стартовая миграция `V1__init.sql`:
    - создание таблиц
    - индексы (включая GIN)
    - ограничение уникальности `slug` среди PUBLISHED можно обеспечить логикой публикации + уникальным индексом по `(slug) WHERE status='PUBLISHED'` (partial index).

## Локальная разработка
- Рекомендуется профили с Testcontainers.
- `SPRING_DATASOURCE_URL`, `USERNAME/PASSWORD` — в `.env`/секретах.
