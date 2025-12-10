#!/usr/bin/env bash
set -euo pipefail

KUBECONFIG_PATH="${KUBECONFIG:-$HOME/.kube/erp-cluster}"
NAMESPACE="${1:-approval}"
ENV_FILE="${ENV_FILE:-../env/secrets}"

if [ ! -f "$KUBECONFIG_PATH" ]; then
  echo "kubeconfig 파일을 찾을 수 없습니다: $KUBECONFIG_PATH" >&2
  exit 1
fi

export KUBECONFIG="$KUBECONFIG_PATH"

echo "[1/4] 네임스페이스 준비 -> $NAMESPACE"
if kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo " - 이미 존재: $NAMESPACE"
else
  kubectl create namespace "$NAMESPACE"
fi
kubectl config set-context --current --namespace="$NAMESPACE"

echo "[2/4] StorageClass 현황"
SC_OUTPUT=$(kubectl get storageclass 2>&1 || true)
if echo "$SC_OUTPUT" | grep -qi "No resources found"; then
  echo " - StorageClass가 없어 local-path-provisioner를 설치합니다."
  kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml
  kubectl patch storageclass local-path -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}' || true
  SC_OUTPUT=$(kubectl get storageclass 2>&1 || true)
fi
if [ -n "$SC_OUTPUT" ]; then
  echo "$SC_OUTPUT"
fi
DEFAULT_SC=$(kubectl get storageclass -o jsonpath='{.items[?(@.metadata.annotations.storageclass\\.kubernetes\\.io/is-default-class=="true")].metadata.name}' 2>/dev/null || true)
if [ -z "$DEFAULT_SC" ]; then
  echo " - 기본 StorageClass가 설정되어 있지 않습니다. StatefulSet 배포 전 하나를 기본으로 지정하세요." >&2
else
  echo " - 기본 StorageClass: $DEFAULT_SC"
fi

echo "[3/4] Secret 정보 생성"
if [ -f "$ENV_FILE" ]; then
  # shellcheck source=/dev/null
  set -a
  source "$ENV_FILE"
  set +a
  kubectl create secret generic mysql-credentials \
    --from-literal=rootPassword="${MYSQL_ROOT_PASSWORD:-root}" \
    --from-literal=username="${DB_USERNAME:-root}" \
    --from-literal=password="${DB_PASSWORD:-root}" \
    --dry-run=client -o yaml | kubectl apply -f -
  kubectl create secret generic mongo-credentials \
    --from-literal=uri="${MONGODB_URI:-mongodb://mongodb:27017/erp}" \
    --dry-run=client -o yaml | kubectl apply -f -
  kubectl create secret generic rabbitmq-credentials \
    --from-literal=username="${RABBITMQ_USERNAME:-guest}" \
    --from-literal=password="${RABBITMQ_PASSWORD:-guest}" \
    --dry-run=client -o yaml | kubectl apply -f -
else
  echo " - ENV_FILE=$ENV_FILE 이 존재하지 않아 Secret 생성을 건너뜁니다." >&2
fi

echo "[4/4] 환경 요약"
kubectl get namespace "$NAMESPACE"
kubectl get secret -n "$NAMESPACE"
