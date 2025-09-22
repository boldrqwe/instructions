# Kubernetes manifests

Текущий деплой пустой: в кластере создаётся только namespace `apps` из файла `namespace.yaml`.

Чтобы добавить сервисы дальше:

- кладите их манифесты в каталог `k8s/`;
- подключайте применение этих файлов в job `deploy` (Cluster smoke) GitHub Actions workflow.

Рекомендация: для каждого сервиса держать отдельные файлы `deployment.yaml`, `service.yaml` и другие необходимые ресурсы.

## Доступ к Kubernetes API

- Если джоба идёт на self-hosted runner, положите в Secrets репозитория `API_SERVER_OVERRIDE=https://127.0.0.1:6443` (или внутренний IP кластера).
- Если деплой выполняется из `ubuntu-latest` (облачный runner), используйте публичный endpoint, доступный из сети GitHub Actions.
