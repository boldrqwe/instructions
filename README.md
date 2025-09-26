# GitOps манифесты для инструкций

Репозиторий содержит Kubernetes-манифесты для развёртывания приложения "Instructions"
в двух окружениях (`test` и `prod`) через Argo CD. В каждом окружении поднимаются три
компонента:

- PostgreSQL из Helm-чарта Bitnami;
- backend (Spring Boot API);
- frontend (React SPA).

## Структура репозитория

```
clusters/
  test/        # корневой Application (app-of-apps) и дочерние Applications для test
  prod/        # корневой Application (app-of-apps) и дочерние Applications для prod
apps/
  backend/     # base + overlays для backend
  frontend/    # base + overlays для frontend
  db/          # overlays с секретами и Argo CD Applications на PostgreSQL
infra/argocd/  # дополнительные объекты Argo CD (проекты и т.п.)
```

### Backend / Frontend

Базовые директории (`apps/<component>/base`) содержат Deployment, Service и Ingress с
минимальными параметрами. В `overlays/test` и `overlays/prod` задаются namespace,
количество реплик и ingress-хосты. Сами образы меняются через секцию `images` в
`kustomization.yaml`.

- Backend слушает порт `8080`, использует секрет `db-auth` и переменные `DB_*` для
  подключения к БД. В обоих окружениях развёрнута 1 реплика.
- Frontend слушает порт `80`, имеет `readinessProbe` на `/`. В `test` — 1 реплика,
  в `prod` — 2 реплики.

### База данных

В директории `apps/db/overlays/<env>` лежит секрет `db-auth` и Argo CD Application,
который разворачивает Bitnami PostgreSQL (release `postgres`) с переопределением имени
сервиса (`fullnameOverride: postgres`). Бэкенд подключается к сервису `postgres:5432`.

## Настройка образов

По умолчанию в overlays используются плейсхолдеры:

- Backend: `ghcr.io/boldrqwe/backend:latest`
- Frontend: `ghcr.io/boldrqwe/frontend:latest`

Чтобы заменить образы, отредактируйте `images` в файлах:

- `apps/backend/overlays/test/kustomization.yaml`
- `apps/backend/overlays/prod/kustomization.yaml`
- `apps/frontend/overlays/test/kustomization.yaml`
- `apps/frontend/overlays/prod/kustomization.yaml`

Например:

```yaml
images:
  - name: backend
    newName: ghcr.io/boldrqwe/backend
    newTag: v1.2.3
```

## Настройка секретов БД

Секрет `db-auth` содержит три ключа: `postgres-password`, `password` (для пользователя
`app`) и `replication-password`. Значения должны быть в Base64. Для генерации можно
воспользоваться `printf 'MyPassword' | base64`.

Обновите файлы:

- `apps/db/overlays/test/secret-db-auth.yaml`
- `apps/db/overlays/prod/secret-db-auth.yaml`

и замените плейсхолдеры на свои значения. Пароли в обоих окружениях могут отличаться.

## Персистентность БД

По умолчанию `primary.persistence.enabled=false`, чтобы можно было быстро стартовать без
PVC. Для включения постоянного хранения в `prod` (или другом окружении) измените блок
`primary.persistence` в `apps/db/overlays/prod/app-postgres.yaml`, например:

```yaml
primary:
  persistence:
    enabled: true
    storageClass: <storage-class>
    size: 10Gi
```

## Развёртывание через Argo CD

1. В Argo CD UI создайте приложение `env-test`:
   - Repo URL: `https://github.com/boldrqwe/k8s.git`
   - Path: `clusters/test`
   - Destination: выбранный кластер, namespace `argocd`
   - Включите автоматическую синхронизацию (prune, self-heal) и опцию `Create Namespace`.
2. Аналогично создайте приложение `env-prod`, указав Path `clusters/prod`.

Корневые приложения синхронизируют дочерние Applications (`db`, `backend`, `frontend`).
После синхронизации сервисы будут доступны по хостам:

- Test: `test.79.174.84.176.sslip.io` (frontend), `api-test.79.174.84.176.sslip.io` (backend)
- Prod: `app.79.174.84.176.sslip.io` (frontend), `api.79.174.84.176.sslip.io` (backend)

## Проверка манифестов

Для локальной проверки соберите манифесты через Kustomize:

```bash
kustomize build apps/backend/overlays/test
kustomize build apps/backend/overlays/prod
kustomize build apps/frontend/overlays/test
kustomize build apps/frontend/overlays/prod
kustomize build apps/db/overlays/test
kustomize build apps/db/overlays/prod
```

Все манифесты должны успешно генерироваться и соответствовать требованиям Kubernetes 1.24+.
