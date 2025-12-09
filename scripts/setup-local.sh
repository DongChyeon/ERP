#!/usr/bin/env bash
set -euo pipefail

check_cmd() { command -v "$1" >/dev/null 2>&1; }

install_mac() {
  if ! check_cmd brew; then
    echo "Homebrew가 필요합니다. https://brew.sh 참고" >&2
    exit 1
  fi
  brew install kubectl helm kustomize
}

install_linux() {
  sudo apt-get update
  sudo apt-get install -y ca-certificates curl gnupg
  if ! check_cmd kubectl; then
    curl -fsSLo kubectl "https://dl.k8s.io/release/$(curl -fsSL https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    sudo install -m 0755 kubectl /usr/local/bin/kubectl
    rm kubectl
  fi
  if ! check_cmd helm; then
    curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
  fi
  if ! check_cmd kustomize; then
    curl -fsSLo kustomize.tar.gz \
      "https://github.com/kubernetes-sigs/kustomize/releases/latest/download/kustomize_v5.4.2_linux_amd64.tar.gz"
    tar -xzf kustomize.tar.gz
    sudo install kustomize /usr/local/bin/kustomize
    rm -f kustomize kustomize.tar.gz
  fi
}

case "$OSTYPE" in
  darwin*) install_mac ;;
  linux*) install_linux ;;
  *) echo "지원하지 않는 OS: $OSTYPE" >&2; exit 1 ;;
esac

mkdir -p "$HOME/.kube"
KUBECONFIG_LOCAL="$HOME/.kube/erp-cluster"
if [ ! -f "$KUBECONFIG_LOCAL" ]; then
  scp "ubuntu@10.0.10.189:/home/ubuntu/.kube/config" "$KUBECONFIG_LOCAL"
  chmod 600 "$KUBECONFIG_LOCAL"
  KUBECONFIG="$KUBECONFIG_LOCAL" kubectl config rename-context kubernetes-admin@kubernetes erp-cluster
  KUBECONFIG="$KUBECONFIG_LOCAL" kubectl config use-context erp-cluster
fi

if ! grep -q "KUBECONFIG=$KUBECONFIG_LOCAL" "$HOME/.bashrc" 2>/dev/null && \
   ! grep -q "KUBECONFIG=$KUBECONFIG_LOCAL" "$HOME/.zshrc" 2>/dev/null; then
  echo "export KUBECONFIG=$KUBECONFIG_LOCAL" >> "$HOME/.bashrc"
  echo "export KUBECONFIG=$KUBECONFIG_LOCAL" >> "$HOME/.zshrc"
fi

echo "kubectl/helm/kustomize 설치와 kubeconfig 동기화 완료"
KUBECONFIG="$KUBECONFIG_LOCAL" kubectl config get-contexts || true
