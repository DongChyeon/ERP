#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
cd "$REPO_ROOT"

KUBECONFIG_PATH="${KUBECONFIG:-$HOME/.kube/erp-cluster}"
NAMESPACE="${1:-approval}"
DEFAULT_MANIFEST_DIR="$REPO_ROOT/k8s/datastores"
MANIFEST_DIR="${MANIFEST_DIR:-$DEFAULT_MANIFEST_DIR}"
if [[ "$MANIFEST_DIR" != /* ]]; then
  MANIFEST_DIR="$REPO_ROOT/$MANIFEST_DIR"
fi
ENV_FILE="${ENV_FILE:-env/secrets}"

if [ ! -f "$KUBECONFIG_PATH" ]; then
  echo "kubeconfig 파일을 찾을 수 없습니다: $KUBECONFIG_PATH" >&2
  exit 1
fi

if [ ! -d "$MANIFEST_DIR" ]; then
  echo "매니페스트 디렉터리를 찾을 수 없습니다: $MANIFEST_DIR" >&2
  exit 1
fi

if [ -f "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

export KUBECONFIG="$KUBECONFIG_PATH"

kubectl config set-context --current --namespace="$NAMESPACE" >/dev/null

echo "[1/2] Secret 확인"
if ! kubectl get secret mysql-credentials >/dev/null 2>&1; then
  echo " - mysql-credentials Secret이 없습니다. bootstrap-cluster.sh를 실행했는지 확인하세요." >&2
  exit 1
fi
if ! kubectl get secret mongo-credentials >/dev/null 2>&1; then
  echo " - mongo-credentials Secret이 없습니다." >&2
  exit 1
fi
if ! kubectl get secret rabbitmq-credentials >/dev/null 2>&1; then
  echo " - rabbitmq-credentials Secret이 없습니다." >&2
  exit 1
fi

echo "[2/2] 공식 이미지 기반 StatefulSet 적용"
kubectl apply -R -f "$MANIFEST_DIR"

echo "배포 상태"
kubectl get pods -n "$NAMESPACE"
