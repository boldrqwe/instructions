#!/usr/bin/env bash
set -u -o pipefail

kubeconfig_path="${1:-}"
if [[ -z "$kubeconfig_path" ]]; then
  kubeconfig_path="${KUBECONFIG:-$HOME/.kube/config}"
fi
api_override="${2:-}"

write_output() {
  local line="$1"
  if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
    echo "$line" >> "$GITHUB_OUTPUT"
  fi
}

finish() {
  local status="$1"
  local server_value="$2"
  write_output "api_ok=$status"
  write_output "server=$server_value"
  echo "[k8s-preflight] api_ok=$status"
  exit 0
}

api_ok=false
current_server=""
effective_server=""

if [[ -z "$kubeconfig_path" ]]; then
  echo "[k8s-preflight] No kubeconfig path provided." >&2
  finish false ""
fi

if [[ ! -f "$kubeconfig_path" ]]; then
  echo "[k8s-preflight] Kubeconfig not found at $kubeconfig_path" >&2
  finish false ""
fi

if ! command -v kubectl >/dev/null 2>&1; then
  echo "[k8s-preflight] kubectl is not available in PATH." >&2
  finish false ""
fi

current_server=$(kubectl --kubeconfig "$kubeconfig_path" config view --minify -o jsonpath='{.clusters[0].cluster.server}' 2>/dev/null || true)
cluster_name=$(kubectl --kubeconfig "$kubeconfig_path" config view --minify -o jsonpath='{.clusters[0].name}' 2>/dev/null || true)

if [[ -z "$current_server" ]]; then
  echo "[k8s-preflight] Unable to determine the current Kubernetes API server from kubeconfig." >&2
  finish false ""
fi

echo "[k8s-preflight] Detected Kubernetes API server: $current_server"

if [[ -n "$api_override" ]]; then
  if [[ -n "$cluster_name" ]]; then
    if kubectl --kubeconfig "$kubeconfig_path" config set-cluster "$cluster_name" --server="$api_override" >/dev/null 2>&1; then
      echo "[k8s-preflight] Overriding API server to: $api_override"
      effective_server="$api_override"
    else
      echo "[k8s-preflight] Failed to override API server using kubectl config set-cluster." >&2
      effective_server="$current_server"
    fi
  else
    echo "[k8s-preflight] Could not resolve cluster name for override." >&2
    effective_server="$current_server"
  fi
else
  effective_server="$current_server"
fi

# Re-read the value in case it was updated by kubectl.
if [[ -z "$effective_server" ]]; then
  effective_server=$(kubectl --kubeconfig "$kubeconfig_path" config view --minify -o jsonpath='{.clusters[0].cluster.server}' 2>/dev/null || true)
fi

echo "[k8s-preflight] Using Kubernetes API server: $effective_server"

server_host=""
if [[ -n "$effective_server" ]]; then
  server_host=$(printf '%s\n' "$effective_server" | sed -E 's#https?://([^:/]+).*#\1#')
fi

curl_success=false
nc_success=false

if ! command -v curl >/dev/null 2>&1; then
  echo "[k8s-preflight] curl is not available; skipping HTTP health check." >&2
else
  for attempt in 1 2 3; do
    echo "[k8s-preflight] curl attempt $attempt: $effective_server/version"
    if curl -sk --connect-timeout 5 "$effective_server/version" >/dev/null 2>&1; then
      curl_success=true
      break
    fi
    if [[ $attempt -lt 3 ]]; then
      sleep 2
    fi
  done
fi

if [[ -n "$server_host" ]]; then
  if command -v nc >/dev/null 2>&1; then
    for attempt in 1 2 3; do
      echo "[k8s-preflight] nc attempt $attempt: $server_host:6443"
      if nc -vz -w 3 "$server_host" 6443; then
        nc_success=true
        break
      fi
      if [[ $attempt -lt 3 ]]; then
        sleep 2
      fi
    done
  else
    echo "[k8s-preflight] nc is not available; skipping TCP connectivity check." >&2
  fi
else
  echo "[k8s-preflight] Could not parse host from server URL '$effective_server'." >&2
fi

if [[ "$curl_success" == true || "$nc_success" == true ]]; then
  api_ok=true
  echo "[k8s-preflight] Kubernetes API appears reachable."
else
  api_ok=false
  echo "Cluster API unreachable ($effective_server), skipped deploy."
  echo "[k8s-preflight] Neither curl nor nc succeeded. Verify network access and API endpoint configuration." >&2
fi

finish "$api_ok" "$effective_server"
