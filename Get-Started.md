# 시작하는 법

이 문서는 새로 프로젝트를 받아 처음 실행하거나 학교 VM 기반의 Kubernetes 클러스터에 배포하기 위한 순서를 정리했습니다. 로컬 Docker Compose 실행과 Kubernetes 배포 두 가지 플로우를 모두 설명합니다.

## 1. 사전 준비

- Docker / Docker Compose (v2)와 Git
- 로컬에서 Kubernetes 클러스터를 조작할 사용자는 `kubectl`, `helm`, `kustomize`, `ssh`가 필요합니다. `scripts/setup-local.sh`가 자동으로 설치해 줍니다.
- 학교 VPN 혹은 내부망에 접속할 수 있는 환경. VPN을 끈 상태로는 VM(예: `10.0.10.189`)에 접근할 수 없습니다.

## 2. 저장소 클론

```bash
git clone https://github.com/DongChyeon/ERP.git
cd 32190473
```

## 3. 공통 환경 변수를 채우기

모든 실행 방식은 `env/secrets` 파일을 참조합니다.

```bash
cp env/secrets.example env/secrets
# 필요한 값 편집
```

| 키 | 설명 |
| --- | --- |
| `MYSQL_ROOT_PASSWORD` | MySQL root 패스워드 (Helm / docker-compose 공통) |
| `DB_USERNAME`, `DB_PASSWORD` | 애플리케이션 DB 계정 (MySQL의 `MYSQL_USER` 용도이며 **root를 넣으면 안 됩니다**; `appuser` 등 별도 사용자로 설정) |
| `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | RabbitMQ 인증 정보 |
| 추가: `MONGODB_URI`, `MONGODB_USERNAME`, `MONGODB_PASSWORD` 등을 Kubernetes용으로 추가 가능 |

## 4. 로컬 Docker Compose 실행

빠르게 전체 스택을 올리고 싶다면 Compose를 사용하세요.

```bash
docker compose up -d mysql mongo rabbitmq
# 초기 스키마 설치 (최초 1회) -> mysql 컨테이너가 scripts/init_mysql.sql을 자동 실행
# 이후 애플리케이션 컨테이너 기동
docker compose up -d employee-service approval-request-service approval-processing-service notification-service
```

확인용으로 `docker compose logs -f employee-service`, `http://localhost:8081/actuator/health` 등을 점검합니다.

## 5. Kubernetes 배포 (학교 VM)

### 5.1 kubeconfig 동기화

로컬 PC 터미널에서 VPN을 켠 뒤 `scripts/setup-local.sh`를 실행하면 `~/.kube/erp-cluster`에 kubeconfig를 복사하고 `kubectl/helm/kustomize`를 설치합니다.

```bash
./scripts/setup-local.sh
source ~/.zshrc # 또는 ~/.bashrc
./scripts/verify-cluster.sh
```

`verify-cluster.sh`는 현재 컨텍스트와 노드 상태를 확인합니다. `dk-32190473` 노드가 `Ready` 여야 합니다.

### 5.2 네임스페이스 · StorageClass · Secret 부트스트랩

```bash
ENV_FILE=env/secrets ./scripts/bootstrap-cluster.sh approval
```

- `approval` 네임스페이스가 없으면 생성
- StorageClass가 없으면 `local-path-provisioner`를 설치하고 기본 StorageClass로 지정
- `env/secrets` 내용을 이용해 `mysql-credentials`, `mongo-credentials`, `rabbitmq-credentials` Secret을 생성

### 5.3 데이터스토어(StatefulSet) 배포

```bash
ENV_FILE=env/secrets ./scripts/deploy-datastores.sh approval
```

`k8s/datastores/` 아래 정의된 공식 이미지 기반 StatefulSet(MySQL/MongoDB/RabbitMQ)을 적용합니다. `DB_USERNAME`/`DB_PASSWORD`는 일반 사용자 전용이므로 root를 쓰지 마세요. `kubectl get pods -n approval`로 모든 Pod가 `Running`인지 확인하세요. 데이터 스키마를 수정하려면 `scripts/init_mysql.sql`을 편집한 뒤 MySQL Pod에서 직접 실행하거나, PVC를 초기화한 상태에서 재배포하면 됩니다.

### 5.4 Ingress / MetalLB

NodePort로 충분하면 기본값 그대로 실행하고, 자체 LoadBalancer IP가 필요하면 `INSTALL_METALLB=true`를 지정해 주세요.

```bash
./scripts/install-ingress.sh
# 또는
INSTALL_METALLB=true METALLB_ADDRESS_POOL=192.168.0.240-192.168.0.250 ./scripts/install-ingress.sh
```

MetalLB를 사용하면 해당 IP 풀을 학교 네트워크에서 사용할 수 있는 범위로 바꿔야 합니다. Ingress를 외부에서 접근하기 위해선 DNS 또는 `/etc/hosts`에 Ingress IP ↔ 원하는 도메인(예: `approval.erp.local`) 매핑이 필요합니다.

### 5.5 애플리케이션 매니페스트 적용

`scripts/deploy-apps.sh`는 각 서비스 디렉터리에서 Gradle 빌드 → Docker 이미지 생성 → (선택적으로) 레지스트리 푸시 → 매니페스트 적용 → `kubectl set image`까지 한 번에 처리합니다. 기본 설정은 `REGISTRY=registry.local/approval`, `IMAGE_TAG=latest`, `BUILD_IMAGES=true`, `PUSH_IMAGES=true`이며, 상황에 따라 환경 변수로 덮어쓸 수 있습니다.

> **사설 레지스트리 준비**<br>
> 학교 VM에 공인 IP가 없으면 VPN으로 접근 가능한 내부 레지스트리를 직접 띄운 뒤 그 주소를 `REGISTRY`에 지정해야 합니다. 예:
> ```bash
> # VM에서 레지스트리 컨테이너 실행 (예: 10.0.10.189:5000)
> docker run -d -p 5000:5000 --name registry registry:2
> ```
> 로컬 Docker 데몬에 `{"insecure-registries": ["10.0.10.189:5000"]}`를 추가하고 재시작하면 `REGISTRY=10.0.10.189:5000/approval`처럼 사용할 수 있습니다. **쿠버네티스 노드(containerd/docker)에도 동일하게 등록**해야 합니다. 예를 들어 containerd라면 `/etc/containerd/config.toml`에 다음을 추가 후 `sudo systemctl restart containerd`:
> ```toml
> [plugins."io.containerd.grpc.v1.cri".registry.mirrors."10.0.10.189:5000"]
>   endpoint = ["http://10.0.10.189:5000"]
> [plugins."io.containerd.grpc.v1.cri".registry.configs."10.0.10.189:5000".tls]
>   insecure_skip_verify = true
> ```
> 이렇게 해야 노드가 HTTP 레지스트리에서 이미지를 pull할 수 있습니다. 다른 레지스트리를 사용 중이라면 해당 서비스의 IP/포트를 `REGISTRY`에 입력하고, 노드 런타임에도 신뢰 구성을 적용하세요.

```bash
# 두 번째 인자로 레지스트리 경로를 직접 넘길 수도 있습니다.
DOCKER_DEFAULT_PLATFORM=linux/amd64 REGISTRY=my-registry.local/approval IMAGE_TAG=$(date +%Y%m%d%H%M%S) MANIFEST_DIR=k8s ./scripts/deploy-apps.sh approval my-registry.local/approval
```

이 명령은 `k8s/kustomization.yaml`을 기반으로 Deployments, Services, Ingress를 모두 적용하고, 지정한 이미지 태그로 각 Deployment를 업데이트합니다. `BUILD_IMAGES=false` 또는 `PUSH_IMAGES=false`로 지정하면 해당 단계를 건너뛰고 `kubectl` 적용만 수행합니다. 배포 후엔 다음 명령으로 상태를 확인하세요.

두 번째 인자로 레지스트리 경로를 넘기면 환경 변수 대신 그 값을 우선 적용합니다(예: `./scripts/deploy-apps.sh approval 10.0.10.189:5000/approval`). ARM 기반 Mac과 같이 호스트 아키텍처가 다를 경우 `DOCKER_DEFAULT_PLATFORM=linux/amd64`를 함께 지정해 Kubernetes 노드와 동일한 이미지 아키텍처를 생성하세요.

```bash
kubectl get pods -n approval
kubectl logs deployment/approval-request-service -n approval
kubectl get ingress -n approval
```

## 6. 이후 유지보수 팁

- 새로 온 개발자는 `Get-Started.md`의 순서를 그대로 따라 하면 로컬 Compose 또는 Kubernetes 환경까지 완성할 수 있습니다.
- CI/CD를 구성할 때는 `env/secrets` 값을 GitHub Secrets에 매핑하고, 빌드된 이미지를 `registry.local/...` 대신 여러분이 접근 가능한 Registry 경로로 푸시한 뒤 `k8s/*.yaml`을 업데이트하세요.
- 데이터베이스 스키마를 변경할 때는 `scripts/init_mysql.sql`을 버전 관리하며, 반드시 개발/테스트 환경에서 먼저 검증하십시오.

필요한 경우 이 문서를 팀 Wiki로 옮기고, 학교 클라우드/네트워크 정책 변화에 따라 VPN·LoadBalancer 설정 단계를 갱신해 주세요.
