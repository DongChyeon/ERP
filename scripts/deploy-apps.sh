#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
cd "$REPO_ROOT"

KUBECONFIG_PATH="${KUBECONFIG:-$HOME/.kube/erp-cluster}"
NAMESPACE="${1:-approval}"
DEFAULT_REGISTRY="${REGISTRY:-registry.local/approval}"
REGISTRY_ARG="${2:-}"
REGISTRY="${REGISTRY_ARG:-$DEFAULT_REGISTRY}"
DEFAULT_MANIFEST_DIR="$REPO_ROOT/k8s"
MANIFEST_DIR="${MANIFEST_DIR:-$DEFAULT_MANIFEST_DIR}"
if [[ "$MANIFEST_DIR" != /* ]]; then
  MANIFEST_DIR="$REPO_ROOT/$MANIFEST_DIR"
fi
IMAGE_TAG="${IMAGE_TAG:-latest}"
BUILD_IMAGES="${BUILD_IMAGES:-true}"
PUSH_IMAGES="${PUSH_IMAGES:-true}"
GRADLE_ARGS="${GRADLE_ARGS:=-x test}"
SERVICES=(
  employee-service
  approval-request-service
  approval-processing-service
  notification-service
)

export DOCKER_DEFAULT_PLATFORM="${DOCKER_DEFAULT_PLATFORM:-linux/amd64}"

if [ ! -f "$KUBECONFIG_PATH" ]; then
  echo "kubeconfig 파일을 찾을 수 없습니다: $KUBECONFIG_PATH" >&2
  exit 1
fi

if [ ! -d "$MANIFEST_DIR" ]; then
  echo "매니페스트 디렉터리를 찾지 못했습니다: $MANIFEST_DIR" >&2
  exit 1
fi

export KUBECONFIG="$KUBECONFIG_PATH"

build_and_push() {
  local service="$1"
  local image="$REGISTRY/$service:$IMAGE_TAG"

echo "[build] $service -> $image"
  if [ ! -d "$service" ]; then
    echo "  - 디렉터리를 찾을 수 없습니다: $service" >&2
    exit 1
  fi

  (cd "$service" && chmod +x gradlew && ./gradlew bootJar $GRADLE_ARGS)
  docker build -t "$image" "$service"

  if [[ "$PUSH_IMAGES" == "true" ]]; then
    docker push "$image"
  fi
}

APPLY_CMD=(kubectl apply)
if [ -f "$MANIFEST_DIR/kustomization.yaml" ] || [ -f "$MANIFEST_DIR/kustomization.yml" ]; then
  APPLY_CMD+=( -k "$MANIFEST_DIR" )
else
  APPLY_CMD+=( -R -f "$MANIFEST_DIR" )
fi

kubectl config set-context --current --namespace="$NAMESPACE" >/dev/null

if [[ "$BUILD_IMAGES" == "true" ]]; then
  echo "[info] REGISTRY=$REGISTRY, IMAGE_TAG=$IMAGE_TAG"
  for svc in "${SERVICES[@]}"; do
    build_and_push "$svc"
  done
else
  echo "[build] BUILD_IMAGES=false 이므로 이미지 빌드/푸시는 건너뜁니다"
fi

echo "[1/3] 매니페스트 적용 (${APPLY_CMD[*]})"
"${APPLY_CMD[@]}"

echo "[2/3] 이미지 업데이트"
for svc in "${SERVICES[@]}"; do
  image="$REGISTRY/$svc:$IMAGE_TAG"
  if kubectl get deployment "$svc" -n "$NAMESPACE" >/dev/null 2>&1; then
    kubectl set image deployment/"$svc" "$svc"="$image" -n "$NAMESPACE"
  else
    echo "  - deployment/$svc 를 찾을 수 없어 이미지 업데이트를 건너뜁니다"
  fi
done

echo "[3/3] 리소스 요약"
kubectl get deploy,svc,ingress -n "$NAMESPACE"
