# Код-стандарты (Backend)

## Пакеты
com.example.instructions
├─ api (controllers, DTO)
├─ service (application services, transactions)
├─ domain (entities, aggregates)
├─ repo (JPA, queries)
├─ security (config, converters)
├─ search (tsvector sync)
├─ common (exceptions, error codes, utils)
└─ config


## Именование
- Контроллеры: `*Controller`, сервисы: `*Service`, репозитории: `*Repository`.
- DTO: `*Request`, `*Response`, соответствуют OpenAPI схемам.

## Ошибки
- Единый `ApiException` с `code` и HTTP-статусом.
- Маппер ошибок в JSON: `{ "code": "...", "message": "..." }`.

## Тесты
- JUnit5 + Testcontainers (Postgres).
- Минимум: happy-path + негативные (401/403/404/409).

## Логи
- Формат JSON, поля: `ts`, `level`, `traceId`, `spanId`, `event`, `articleId`, `version` (если есть).

## Изменение контракта
- Запрещено без ADR и правки `/docs/api/openapi.yaml`.
