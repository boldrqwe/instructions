# Instructions

Полноценный пример приложения для публикации и редактирования инструкций.
Бэкенд реализован на Spring Boot, данные хранятся в PostgreSQL. Фронтенд
создан на React + TypeScript и использует богатый текстовый редактор
(react-quill), позволяющий форматировать статьи перед сохранением.

## Состав проекта

- `backend` — REST API на Spring Boot 3 (Java 17, Spring Data JPA, Bean Validation).
- `frontend` — одностраничное приложение на Vite + React + TypeScript.
- `docker-compose.yml` — инфраструктура из PostgreSQL, бэкенда и собранного фронтенда.

## Запуск локально

### Backend

```bash
cd backend
mvn spring-boot:run
```

Доступные переменные окружения:

- `DB_HOST` (по умолчанию `localhost`)
- `DB_PORT` (по умолчанию `5432`)
- `DB_NAME` (по умолчанию `instructions`)
- `DB_USERNAME` (по умолчанию `postgres`)
- `DB_PASSWORD` (по умолчанию `postgres`)
- `CORS_ALLOWED_ORIGINS` (по умолчанию `http://localhost:5173`)

При первом запуске создаются две демонстрационные статьи.
REST API доступно по адресу `http://localhost:8080/api/articles`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

По умолчанию фронтенд отправляет запросы на `http://localhost:8080`.
Можно переопределить адрес API, задав переменную `VITE_API_BASE_URL`.

## Docker Compose

Для проверки полного стека выполните:

```bash
docker compose up --build
```

Сервисы и порты:

- PostgreSQL: `localhost:5432`
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:3000`

При необходимости можно переопределить переменные окружения Compose:
`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `CORS_ALLOWED_ORIGINS`,
`VITE_API_BASE_URL`.

## Деплой на Railway / в Docker

- Бэкенд имеет Dockerfile и запускается командой
  `java $JAVA_OPTS -jar /app/app.jar`. Railway автоматически определит порт через переменную `PORT`.
- Фронтенд собирается в статический бандл и отдаётся nginx. При сборке можно
  переопределить `VITE_API_BASE_URL`, чтобы прописать URL REST API в бандл.
- Для production-деплоя достаточно выполнить `docker build` для каждой части
  или использовать `docker compose build`/`push`.

## API

| Метод | Маршрут                 | Описание                 |
|-------|------------------------|--------------------------|
| GET   | `/api/articles`        | Список всех статей       |
| GET   | `/api/articles/{id}`   | Получение статьи по id   |
| POST  | `/api/articles`        | Создание новой статьи    |
| PUT   | `/api/articles/{id}`   | Обновление существующей  |
| DELETE| `/api/articles/{id}`   | Удаление статьи          |

Пример тела запроса:

```json
{
  "title": "Как развернуть проект",
  "content": "<p>Подробное описание с форматированием…</p>"
}
```

Ошибки возвращаются в формате:

```json
{
  "timestamp": "2024-03-19T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Данные заполнены некорректно",
  "details": ["title: Заголовок обязателен"]
}
```

## Тестирование

- `mvn test` — интеграционные тесты REST API (используется H2 в режиме PostgreSQL).
- `npm run lint` и `npm run build` — проверка фронтенда.

## Структура фронтенда

Интерфейс содержит панель со списком статей и редактор. Для текста доступно:
заголовки, списки, выделение, цитаты, блоки кода, ссылки, изображения и видео.
После сохранения статья обновляется в списке без перезагрузки страницы.

## Скриншоты

Фронтенд генерирует современный UI (см. директорию `frontend/src`). При необходимости
можно подключить собственную тему или заменить редактор на другой.
