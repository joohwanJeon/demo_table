# 캐치테이블 데모 — 예약 오픈런 시나리오

VM(Nuxt3 on EC2) + 컨테이너(Spring Boot on K8s) 하이브리드 구조의 데모 앱..
"인기 식당 예약 오픈 → 트래픽 폭증"을 재현해 **로드밸런싱 / HPA 오토스케일링 / Self-healing**을 시연하기 위한 수단입니다.

> 앱은 평가 대상이 아니라 인프라를 검증하는 도구입니다. 기능은 목록/상세/예약(=주문) 3종이면 충분합니다.

## 폴더 구조

```
catchtable-demo/
├── backend/      Spring Boot 예약 API (컨테이너 → K8s에 배포)
│   ├── src/...   Restaurant/Reservation API + /api/health + /api/load
│   └── Dockerfile (메모리 함정 방지 JVM 옵션 포함)
├── frontend/     Nuxt3 프론트 (EC2에서 실행 = VM 계층)
└── k8s/          제출용 매니페스트 4종
    ├── deployment.yaml
    ├── service.yaml
    ├── ingress.yaml   (AWS ALB)
    └── hpa.yaml
```

## API 한눈에 보기

| 메서드 | 경로 | 설명 | RFP 대응 |
|---|---|---|---|
| GET | `/api/restaurants` | 식당 목록 | 상품 목록 조회 |
| GET | `/api/restaurants/{id}` | 식당 상세 | 상품 상세 조회 |
| POST | `/api/reservations` | 예약하기 | 주문 요청 |
| GET | `/api/health` | 헬스체크 (probe·ALB용) | — |
| GET | `/api/load?ms=300` | **HPA 부하 시연 전용** CPU 소모 | — |

모든 응답에 `servedBy`(응답한 Pod 이름)가 들어 있어 **로드밸런싱 증적**으로 씁니다.

---

## 먼저 바꿔야 할 곳 (TODO 3개)

1. `k8s/deployment.yaml` → `image:` 를 **본인 ECR 주소**로
2. `k8s/deployment.yaml` → `SPRING_DATASOURCE_URL` 의 `<RDS_ENDPOINT>` 를 **본인 RDS 주소**로
3. DB 접속 정보를 Secret으로 생성 (아래)

```bash
kubectl create secret generic db-secret \
  --from-literal=username=admin \
  --from-literal=password='본인_RDS_비밀번호'
```

---

## 1) 백엔드 빌드 → ECR Push

```bash
cd backend

# ECR 로그인 (리전/계정ID는 본인 값)
aws ecr get-login-password --region ap-northeast-2 \
  | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com

# ECR 레포지토리 생성 (최초 1회)
aws ecr create-repository --repository-name reservation-api --region ap-northeast-2

# 빌드 & 태그 & 푸시
docker build -t reservation-api .
docker tag reservation-api:latest <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/reservation-api:latest
docker push <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/reservation-api:latest
```

## 2) K8s 배포

```bash
# metrics-server (HPA에 필수) — 없으면 HPA의 CPU가 <unknown> 으로 뜸
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

cd ../k8s
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml   # AWS Load Balancer Controller 필요
kubectl apply -f hpa.yaml

kubectl get pods -w            # Running 될 때까지 (Spring Boot라 30초쯤 걸림)
kubectl get ingress           # ALB 주소(ADDRESS) 확인
```

> ALB Controller가 없다면 `service.yaml`의 `type`을 `LoadBalancer`로 바꾸고 `ingress.yaml`은 생략하세요.

## 3) 프론트(Nuxt3)를 EC2에서 실행 = VM 계층

```bash
cd frontend
npm install

# 백엔드(ALB) 주소를 환경변수로 지정
export NUXT_PUBLIC_API_BASE="http://<ALB_주소>"

# 데모용 간단 실행
npm run dev -- --host        # 또는: npm run build && npm run preview
```

---

## 증적 3종 찍는 법

### A. 로드밸런싱
프론트에서 **새로고침** 버튼을 반복 클릭 → `응답한 Pod` 값이 바뀌면 여러 Pod로 분산되는 증거.
터미널이면:
```bash
for i in $(seq 1 10); do curl -s http://<ALB>/api/health | grep -o '"servedBy":"[^"]*"'; done
```

### B. HPA 오토스케일링 (예약 오픈런 재현)
한 터미널에서 HPA를 지켜보고:
```bash
kubectl get hpa -w
```
다른 터미널에서 부하를 쏟아부음 (예약 오픈 순간 재현):
```bash
# hey 사용 예 (ab, k6 무엇이든 OK)
hey -z 120s -c 50 "http://<ALB>/api/load?ms=400"
```
→ CPU가 70%를 넘으면 Pod가 2 → 3 → 5개로 증가, 부하가 끝나면 다시 감소.

### C. Self-healing
```bash
kubectl get pods
kubectl delete pod <pod-이름>      # Pod 하나 강제 삭제
kubectl get pods -w               # 자동으로 새 Pod가 생성되어 2개 유지되는지 확인
```
이때 프론트는 계속 정상 응답해야 함(다른 Pod가 받아줌).

---

## 로컬에서 먼저 돌려보고 싶다면
MySQL만 로컬에 띄우면 `backend`는 `./gradlew bootRun`(혹은 `gradle bootRun`)으로,
`frontend`는 `npm run dev`로 바로 확인됩니다. (application.yml 기본값이 localhost MySQL)
