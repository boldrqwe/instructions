# Kubernetes manifests

Текущий деплой пустой: в кластере создаётся только namespace `apps` из файла `namespace.yaml`.

Чтобы добавить сервисы дальше:

- кладите их манифесты в каталог `k8s/`;
- подключайте применение этих файлов в job `deploy` (Cluster smoke) GitHub Actions workflow.

Рекомендация: для каждого сервиса держать отдельные файлы `deployment.yaml`, `service.yaml` и другие необходимые ресурсы.
