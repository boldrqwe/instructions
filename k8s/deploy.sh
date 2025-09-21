#!/usr/bin/env bash
# deploy.sh — идемпотентный деплой манифестов из k8s/ с комментариями.
# Использование:
#   ./deploy.sh                 # обычный деплой
#   ./deploy.sh --dry-run       # покажет diff/валидацию без применения
#
# Требования:
#   - kubectl настроен на нужный контекст (kubeconfig)
#   - директория k8s/ с yaml-файлами как в проекте
#
# Полезные ссылки:
#   kubectl apply: https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#apply
#   rollout status: https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#checking-rollout-status

set -euo pipefail

# === Параметры (можно переопределить env-переменными) =========================
APP_NS="${APP_NS:-apps}"               # ns для приложения
KAFKA_NS="${KAFKA_NS:-kafka}"          # ns для Kafka/Strimzi
MANIFEST_DIR="${MANIFEST_DIR:-k8s}"    # где лежат yaml
WAIT_ROLLOUT="${WAIT_ROLLOUT:-true}"   # ждать ли завершения rollout’а
DEPLOYMENT_NAME="${DEPLOYMENT_NAME:-my-service}"  # имя Deployment для ожидания

DRY_RUN="${1:-}"                        # поддержка флага --dry-run (визуальная проверка)
# ==============================================================================

# Цветной вывод (для удобства)
green(){ echo -e "\033[32m$*\033[0m"; }
yellow(){ echo -e "\033[33m$*\033[0m"; }
red(){ echo -e "\033[31m$*\033[0m"; }

# Проверка наличия kubectl и доступа к кластеру
check_kubectl(){
  if ! command -v kubectl >/dev/null 2>&1; then
    red "kubectl не найден в PATH"; exit 1
  fi
  if ! kubectl version --short >/dev/null 2>&1; then
    red "kubectl не может подключиться к кластеру (проверь kubeconfig/kubecontext)"; exit 1
  fi
  yellow "Контекст: $(kubectl config current-context || echo 'unknown')"
}

# Создать namespace, если его нет (idempotent)
ensure_ns(){
  local ns="$1"
  if kubectl get ns "$ns" >/dev/null 2>&1; then
    green "Namespace '$ns' уже существует"
  else
    yellow "Создаю namespace '$ns'..."
    kubectl create ns "$ns"
  fi
}

# Удобный wrapper для apply с опцией dry-run
apply_file(){
  local file="$1"
  if [[ -n "$DRY_RUN" && "$DRY_RUN" == "--dry-run" ]]; then
    # Показываем diff (если поддерживается) и валидацию, но не применяем
    yellow "[DRY-RUN] kubectl diff -f $file"
    kubectl diff -f "$file" || true   # diff возвращает 1 при отличиях — не считаем за ошибку
  else
    yellow "kubectl apply -f $file"
    kubectl apply -f "$file"
  fi
}

# Проверить, что Strimzi CRDs доступны (KafkaTopic и Kafka)
strimzi_ready(){
  kubectl api-resources | grep -q "^kafkatopics" || return 1
  kubectl api-resources | grep -q "^kafkas" || return 1
  return 0
}

# Проверить наличие Kafka-кластера (например, my-cluster)
kafka_cluster_exists(){
  local name="${1:-my-cluster}"
  kubectl -n "$KAFKA_NS" get kafka "$name" >/dev/null 2>&1
}

main(){
  check_kubectl

  # 0) базовые пространства имён (apps/kafka/observability)
  apply_file "${MANIFEST_DIR}/namespaces.yaml"
  ensure_ns "$APP_NS"
  ensure_ns "$KAFKA_NS"

  # 1) RBAC для деплой-аккаунта (в ns apps)
  apply_file "${MANIFEST_DIR}/rbac-deployer.yaml"

  # 2) ConfigMap (безопасно хранить в Git)
  apply_file "${MANIFEST_DIR}/configmap.yaml"

  # 3) Secret — ВНИМАНИЕ: реальный секрет создаётся отдельной командой.
  # В репозитории можно держать только шаблон 'secret.example.yaml' ради структуры.
  if [[ -f "${MANIFEST_DIR}/secret.yaml" ]]; then
    # Если ты назвал реальный файл secret.yaml — применим.
    apply_file "${MANIFEST_DIR}/secret.yaml"
  else
    yellow "Секреты не применяю (ожидается ручное создание real secret через 'kubectl create secret ...')."
  fi

  # 4) Приложение: Deployment + Service + Ingress + HPA + PDB + NetworkPolicy
  apply_file "${MANIFEST_DIR}/deployment.yaml"
  apply_file "${MANIFEST_DIR}/service.yaml"
  [[ -f "${MANIFEST_DIR}/ingress.yaml" ]] && apply_file "${MANIFEST_DIR}/ingress.yaml"
  [[ -f "${MANIFEST_DIR}/hpa.yaml"     ]] && apply_file "${MANIFEST_DIR}/hpa.yaml"
  [[ -f "${MANIFEST_DIR}/pdb.yaml"     ]] && apply_file "${MANIFEST_DIR}/pdb.yaml"
  [[ -f "${MANIFEST_DIR}/networkpolicy.yaml" ]] && apply_file "${MANIFEST_DIR}/networkpolicy.yaml"

  # 5) Ждать ли успешный rollout деплоймента (полезно для CI/быстрой проверки)
  if [[ "$WAIT_ROLLOUT" == "true" && -z "$DRY_RUN" ]]; then
    yellow "Ждём успешный rollout Deployment/${DEPLOYMENT_NAME} в ns ${APP_NS}..."
    kubectl -n "$APP_NS" rollout status deploy/"$DEPLOYMENT_NAME" --timeout=120s
  fi

  # 6) Kafka Topics — применяем только если Strimzi установлен и кластер Kafka существует
  if [[ -f "${MANIFEST_DIR}/kafka-topics.yaml" ]]; then
    if strimzi_ready && kafka_cluster_exists "my-cluster"; then
      apply_file "${MANIFEST_DIR}/kafka-topics.yaml"
    else
      yellow "Пропускаю kafka-topics.yaml: Strimzi/Kafka-кластер недоступны (или другой неймспейс/имя)."
      yellow "Проверь, что оператор Strimzi установлен и Kafka (например, 'my-cluster') создана в ns '${KAFKA_NS}'."
    fi
  fi

  green "Готово ✅"
}

main "$@"
