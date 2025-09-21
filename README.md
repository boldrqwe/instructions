# instructions

Приложение состоит из бэкенда на Spring Boot и фронтенда на React.

## Backend

```bash
cd backend
mvn spring-boot:run
```

При запуске Liquibase создаёт таблицу `instructions` и наполняет её двумя тестовыми инструкциями.
REST API доступен по адресу `http://localhost:8080/api/instructions`.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Фронтенд стартует на `http://localhost:5173` и загружает инструкции с бэкенда.
При необходимости можно задать переменную `VITE_API_BASE_URL`, чтобы переопределить адрес API.

## Docker Compose

Для запуска инфраструктуры в Docker можно воспользоваться `docker-compose.yaml` в корне репозитория. Он поднимает PostgreSQL, Adminer и фронтенд-девсервер.

```bash
export POSTGRES_DB=instructions
export POSTGRES_USER=instructions
export POSTGRES_PASSWORD=instructions
docker compose up
```

По умолчанию фронтенд проксирует запросы к бэкенду по адресу `http://host.docker.internal:8080`. При необходимости можно переопределить порт фронтенда (`FRONTEND_PORT`) и базовый URL API (`VITE_API_BASE_URL`).

Приложение состоит из бэкенда на Spring Boot и фронтенда на React.

## Backend

```bash
cd backend
mvn spring-boot:run
```

При запуске Liquibase создаёт таблицу `instructions` и наполняет её двумя тестовыми инструкциями.
REST API доступен по адресу `http://localhost:8080/api/instructions`.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Фронтенд стартует на `http://localhost:5173` и загружает инструкции с бэкенда.
При необходимости можно задать переменную `VITE_API_BASE_URL`, чтобы переопределить адрес API.
