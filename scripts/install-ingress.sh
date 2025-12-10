#!/usr/bin/env bash
set -euo pipefail

KUBECONFIG_PATH="${KUBECONFIG:-$HOME/.kube/erp-cluster}"
NAMESPACE="${NAMESPACE:-ingress-nginx}"
INSTALL_METALLB="${INSTALL_METALLB:-true}"
METALLB_NAMESPACE="${METALLB_NAMESPACE:-metallb-system}"
METALLB_ADDRESS_POOL="${METALLB_ADDRESS_POOL:-192.168.0.240-192.168.0.250}"

if [ ! -f "$KUBECONFIG_PATH" ]; then
  echo "kubeconfig 파일을 찾을 수 없습니다: $KUBECONFIG_PATH" >&2
  exit 1
fi

export KUBECONFIG="$KUBECONFIG_PATH"

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx >/dev/null 2>&1 || true
helm repo update >/dev/null

echo "[1/2] NGINX Ingress Controller 설치"
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace "$NAMESPACE" --create-namespace \
  --set controller.metrics.enabled=true \
  --set controller.service.type=LoadBalancer \
  --set controller.service.nodePorts.http=${INGRESS_HTTP_NODEPORT:-30080} \
  --set controller.service.nodePorts.https=${INGRESS_HTTPS_NODEPORT:-30443}

if [[ "$INSTALL_METALLB" == "true" ]]; then
  echo "[2/2] MetalLB 설치"
  kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.13.12/config/manifests/metallb-native.yaml
  kubectl wait --namespace "$METALLB_NAMESPACE" --for=condition=available deployment/controller --timeout=120s || true
  cat <<YAML | kubectl apply -f -
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: local-pool
  namespace: $METALLB_NAMESPACE
spec:
  addresses:
    - $METALLB_ADDRESS_POOL
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: local-advertisement
  namespace: $METALLB_NAMESPACE
spec:
  ipAddressPools:
    - local-pool
YAML
else
  echo "[2/2] MetalLB 설치는 건너뜀 (INSTALL_METALLB=false)"
fi

echo "Ingress Controller 요약"
kubectl get svc -n "$NAMESPACE"
