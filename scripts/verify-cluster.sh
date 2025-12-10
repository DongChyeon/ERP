#!/usr/bin/env bash
set -euo pipefail

KUBECONFIG_PATH="${KUBECONFIG:-$HOME/.kube/erp-cluster}"

if [ ! -f "$KUBECONFIG_PATH" ]; then
  echo "kubeconfig 파일을 찾을 수 없습니다: $KUBECONFIG_PATH" >&2
  echo "스크립트를 실행하기 전에 setup-local.sh로 kubeconfig를 동기화하거나 KUBECONFIG 환경변수를 지정하세요." >&2
  exit 1
fi

export KUBECONFIG="$KUBECONFIG_PATH"

echo "[1/3] 현재 컨텍스트"
kubectl config current-context || { echo "컨텍스트 조회 실패" >&2; exit 1; }

echo "[2/3] 클러스터/사용자 요약"
kubectl config view --minify -o jsonpath='{.contexts[0].context.cluster}{" / "}{.contexts[0].context.user}{"\n"}'

echo "[3/3] 노드 상태"
kubectl get nodes -o wide
