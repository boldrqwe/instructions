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
