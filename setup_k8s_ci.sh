#!/usr/bin/env bash
set -euo pipefail

# ===============================
#  K8s CI kubeconfig generator
#  (c) Egor + ChatGPT
# ===============================

# Defaults (можно переопределить флагами или env)
NS="${NS:-prod-app}"
SA="${SA:-gha-deployer}"
ROLE="${ROLE:-gha-deploy-role}"
KCFG="${KCFG:-$HOME/my-${SA}.kubeconfig}"
SECRET_NAME="${SECRET_NAME:-${SA}-token}"
ADD_READ_NS="${ADD_READ_NS:-false}"            # true -> выдаст кластерные права только на чтение namespaces
API_SERVER_OVERRIDE="${API_SERVER_OVERRIDE:-}" # например: https://api.mycorp.example:6443
PRINT_B64="${PRINT_B64:-false}"                # true -> печатать base64 в stdout (осторожно с логами!)
WRITE_B64_FILE="${WRITE_B64_FILE:-$KCFG.b64}" # файл с base64
SET_GH_SECRET="${SET_GH_SECRET:-false}"        # true -> попробуем записать в GitHub secrets
GH_SECRET_NAME="${GH_SECRET_NAME:-KUBE_CONFIG}"

# ------------- utils -------------
log()   { printf '[%s] %s\n' "$(date +'%F %T')" "$*"; }
warn()  { printf '\033[33m[%s] WARN: %s\033[0m\n' "$(date +'%F %T')" "$*" >&2; }
error() { printf '\033[31m[%s] ERROR: %s\033[0m\n' "$(date +'%F %T')" "$*" >&2; exit 1; }

need_bin() {
  command -v "$1" >/dev/null 2>&1 || error "binary '$1' not found in PATH"
}

usage() {
cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --ns NAME                  Namespace (default: $NS)
  --sa NAME                  ServiceAccount name (default: $SA)
  --role NAME                Role name (default: $ROLE)
  --kcfg PATH                Output kubeconfig path (default: $KCFG)
  --add-read-ns              Also grant cluster-scope read for namespaces (default: $ADD_READ_NS)
  --api-server URL           Override API server URL in kubeconfig (default: none)
  --print-b64                Print base64 kubeconfig to stdout (default: $PRINT_B64)
  --b64-file PATH            Where to save base64 (default: $WRITE_B64_FILE)
  --set-gh-secret            Write to GitHub Secret via 'gh' (default: $SET_GH_SECRET)
  --gh-secret-name NAME      Secret name (default: $GH_SECRET_NAME)
  -h, --help                 Show this help

Env vars mirror flags (NS, SA, ROLE, KCFG, ADD_READ_NS, API_SERVER_OVERRIDE, PRINT_B64, WRITE_B64_FILE, SET_GH_SECRET, GH_SECRET_NAME).
USAGE
}

# ------------- args -------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --ns)                NS="$2"; shift 2;;
    --sa)                SA="$2"; shift 2;;
    --role)              ROLE="$2"; shift 2;;
    --kcfg)              KCFG="$2"; shift 2;;
    --add-read-ns)       ADD_READ_NS=true; shift;;
    --api-server)        API_SERVER_OVERRIDE="$2"; shift 2;;
    --print-b64)         PRINT_B64=true; shift;;
    --b64-file)          WRITE_B64_FILE="$2"; shift 2;;
    --set-gh-secret)     SET_GH_SECRET=true; shift;;
    --gh-secret-name)    GH_SECRET_NAME="$2"; shift 2;;
    -h|--help)           usage; exit 0;;
    *)                   error "unknown arg: $1";;
  esac
done

# ------------- preflight -------------
need_bin kubectl
need_bin base64

if [[ "$SET_GH_SECRET" == "true" ]]; then
  if ! command -v gh >/dev/null 2>&1; then
    warn "'gh' not found; will skip setting GitHub secret"
    SET_GH_SECRET=false
  fi
fi

log "Params: NS=$NS SA=$SA ROLE=$ROLE KCFG=$KCFG"
[[ -n "${API_SERVER_OVERRIDE}" ]] && log "API server override: ${API_SERVER_OVERRIDE}"
log "ADD_READ_NS=$ADD_READ_NS PRINT_B64=$PRINT_B64 SET_GH_SECRET=$SET_GH_SECRET"

# ------------- ensure namespace -------------
if ! kubectl get ns "$NS" >/dev/null 2>&1; then
  log "Creating namespace $NS"
  kubectl create namespace "$NS"
else
  log "Namespace $NS already exists"
fi

# ------------- create SA -------------
log "Applying ServiceAccount $SA in $NS"
kubectl -n "$NS" apply -f - <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ${SA}
EOF

# ------------- Role (namespaced minimal) -------------
log "Applying Role $ROLE (namespaced permissions)"
kubectl -n "$NS" apply -f - <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: ${ROLE}
rules:
  - apiGroups: [""]
    resources: ["pods","services","endpoints","configmaps","secrets","events"]
    verbs: ["get","list","watch","create","update","patch","delete"]
  - apiGroups: ["apps"]
    resources: ["deployments","replicasets","statefulsets","daemonsets"]
    verbs: ["get","list","watch","create","update","patch","delete"]
  - apiGroups: ["batch"]
    resources: ["jobs","cronjobs"]
    verbs: ["get","list","watch","create","update","patch","delete"]
  - apiGroups: ["networking.k8s.io"]
    resources: ["ingresses"]
    verbs: ["get","list","watch","create","update","patch","delete"]
EOF

# ------------- RoleBinding -------------
log "Binding Role $ROLE to SA $SA"
kubectl -n "$NS" apply -f - <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: ${ROLE}-binding
subjects:
  - kind: ServiceAccount
    name: ${SA}
    namespace: ${NS}
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: ${ROLE}
EOF

# ------------- cluster info -------------
log "Reading current kubectl context cluster info"
CLUSTER_NAME="$(kubectl config view --minify -o jsonpath='{.clusters[0].name}')"
SERVER="$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}')"
CA_DATA="$(kubectl config view --raw --minify -o jsonpath='{.clusters[0].cluster.certificate-authority-data}')"

log "Cluster: ${CLUSTER_NAME}"
log "Server (current): ${SERVER}"
if [[ -n "${API_SERVER_OVERRIDE}" ]]; then
  log "Server will be overridden to: ${API_SERVER_OVERRIDE}"
  SERVER="${API_SERVER_OVERRIDE}"
fi

# ------------- SA token secret (stable for CI) -------------
log "Creating token Secret ${SECRET_NAME} for SA ${SA}"
kubectl -n "$NS" apply -f - <<EOF
apiVersion: v1
kind: Secret
type: kubernetes.io/service-account-token
metadata:
  name: ${SECRET_NAME}
  annotations:
    kubernetes.io/service-account.name: "${SA}"
EOF

# дождёмся, пока apiserver заполнит секрет
log "Waiting for token to be populated..."
for i in {1..30}; do
  TOK_B64="$(kubectl -n "$NS" get secret "${SECRET_NAME}" -o jsonpath='{.data.token}' || true)"
  if [[ -n "${TOK_B64}" ]]; then break; fi
  sleep 1
done
[[ -n "${TOK_B64:-}" ]] || error "token not populated in secret ${SECRET_NAME}"
SA_TOKEN="$(printf '%s' "$TOK_B64" | base64 -d)"

# ------------- generate kubeconfig -------------
log "Writing kubeconfig to ${KCFG}"
mkdir -p "$(dirname "$KCFG")"
cat > "$KCFG" <<EOF
apiVersion: v1
kind: Config
clusters:
- name: ${CLUSTER_NAME}
  cluster:
    server: ${SERVER}
    certificate-authority-data: ${CA_DATA}
contexts:
- name: ${CLUSTER_NAME}-${NS}-${SA}
  context:
    cluster: ${CLUSTER_NAME}
    namespace: ${NS}
    user: ${SA}
current-context: ${CLUSTER_NAME}-${NS}-${SA}
users:
- name: ${SA}
  user:
    token: ${SA_TOKEN}
EOF
chmod 600 "$KCFG"

# ------------- smoke tests (namespaced) -------------
log "Auth check (namespaced) ..."
if KUBECONFIG="$KCFG" kubectl -n "$NS" auth can-i get pods >/dev/null 2>&1; then
  log "can-i get pods: OK"
else
  warn "can-i get pods: NO"
fi

log "Listing Deployments in $NS (should work if there are any)"
if ! KUBECONFIG="$KCFG" kubectl -n "$NS" get deploy >/dev/null 2>&1; then
  warn "get deploy failed (maybe none exist yet) — this can be OK"
fi

# ------------- optional cluster-scope read namespaces -------------
if [[ "$ADD_READ_NS" == "true" ]]; then
  log "Granting cluster-scope read for namespaces"
  kubectl apply -f - <<'EOF'
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: gha-read-namespaces
rules:
  - apiGroups: [""]
    resources: ["namespaces"]
    verbs: ["get","list","watch"]
EOF

  kubectl apply -f - <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: gha-read-namespaces-binding-${NS}-${SA}
subjects:
  - kind: ServiceAccount
    name: ${SA}
    namespace: ${NS}
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: gha-read-namespaces
EOF

  log "Cluster-scope test: kubectl get ns"
  if ! KUBECONFIG="$KCFG" kubectl get ns >/dev/null 2>&1; then
    warn "cluster-scope 'get ns' still fails — check apiserver reachability / firewall"
  else
    log "cluster-scope 'get ns': OK"
  fi
fi

# ------------- base64 encode -------------
log "Encoding kubeconfig to base64 (no newlines) -> ${WRITE_B64_FILE}"
base64 -w0 "$KCFG" > "$WRITE_B64_FILE"
chmod 600 "$WRITE_B64_FILE"
HEAD="$(head -c 80 "$WRITE_B64_FILE" || true)"
log "Base64 preview: ${HEAD}... (truncated)"

if [[ "$PRINT_B64" == "true" ]]; then
  warn "Printing FULL base64 kubeconfig to stdout — THIS MAY LEAK IN LOGS!"
  cat "$WRITE_B64_FILE"
fi

# ------------- GitHub secret (optional) -------------
if [[ "$SET_GH_SECRET" == "true" ]]; then
  log "Setting GitHub secret ${GH_SECRET_NAME} via gh CLI"
  gh secret set "${GH_SECRET_NAME}" < "$WRITE_B64_FILE" || warn "gh secret set failed"
  log "GitHub secret ${GH_SECRET_NAME} set (if gh was authenticated)"
fi

log "All done ✅"
log "Use in GitHub Actions:
  - name: Configure kubectl
    run: |
      mkdir -p ~/.kube
      echo \"\${{ secrets.${GH_SECRET_NAME} }}\" | base64 -d > ~/.kube/config
      chmod 600 ~/.kube/config
"
