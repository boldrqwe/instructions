# ADR-003: Поиск на Postgres tsvector

- **Решение:** tsvector + GIN индекс для title/markdown (только по PUBLISHED).
- **Почему:** быстро стартовать без внешнего кластера.
- **План B:** миграция на OpenSearch при росте.
