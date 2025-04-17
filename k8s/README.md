# Kubernetes

## 1 ) Kubernetes란?
- 컨테이너화된 애플리케이션을 자동으로 배포, 확장, 관리해주는 오픈소스 **컨테이너 오케스트레이션 플랫폼**
  - 요약 : 컨테이너를 관리하고 운영하는 자동화된 플랫폼

### 1 - 1 ) Docker 와 Kubernetes 비교
| 항목           | Docker 사용                           | k8s 사용                                  |
|----------------|----------------------------------------|--------------------------------------------|
| 배포           | 수동 배포 또는 Compose 사용            | Deployment로 자동화된 배포 가능            |
| 복구           | 수동 재시작 필요                       | 자동 장애 복구 지원                        |
| 스케일링       | 수동 컨테이너 수 조절                  | 자동/수동 스케일링(HPA 등) 지원            |
| 업데이트       | 직접 재배포 필요                       | 무중단 롤링 업데이트 가능                  |
| 트래픽 라우팅  | 직접 포트 매핑 또는 Nginx 구성 필요     | Service/Ingress로 트래픽 관리 용이         |

## 2 ) 주요 기능
| 기능         | 설명                             |
|------------|--------------------------------|
| 자동 재시작     | 어플리케이션이 죽으면 자동 재실행             |
| 로드 밸런싱     | 여러개의 컨테이너로 트래픽 분산              |
| 롤링 업데이트    | 무중단 배포                         |
| 자동 복구      | 장애 발생 시 자동 복원                  |
| 수평 스케일링    | 부하에 따라 인스턴스 수 조절               |
| 보안 및 구성 분리 | secret, configMap등 사용하여 설정 중앙화 |

## 3 ) 주요 구성 요소
| 구성 요소              | 역할                                          |
|--------------------|---------------------------------------------|
| Pod                | 컨테이너가 실행되는 **최소 단위**                        |
| Node               | Pod가 배치되는 서버 (VM or 물리 서버를 의미)              |
| Deployment         | 어플리케이션의 배포 전략 ( 스케일 업, 다운)                  |
| Service            | Pod의 집합에 **접근할 수 있게 해주는** 가상 IP             |
| Ingress            | 외부 Http/Https 요청을 내부 서비스로 연결 ( Gateway 역할 ) |
| Secret / ConfigMap | 설정 정보를 작성                                   |
| Namespace          | 리소스를 그룹화해서 분리 관리                            |

## 4 ) Spring Cloud 와 대조
- Eureka(Discovery) Service -> Service
- Spring  Cloud Gateway Service -> Service / Ingress
- Spring Config Service -> ConfigMap / Secret

## 5 ) 기본 명령어
- Kubernetes 노드 확인
  - `kubeclt get node`
- Kubernetes 지정 노드의 리소스 확인
  - `kubectl describe node {node-namde}`
- 지정 리소스를 생성 ( 존재할 경우 update 함 )
  - `kubectl apply -f {target.yml}`
- configmap 리소스 정보 확인
  - `kubectl get configmap`

## 6 ) ConfigMap
- 설정 정보를 key-value 형태로 저장 및 설정 파일로 사용 가능

```yaml
apiVersion: v1               # API 버전 (ConfigMap은 항상 v1 사용)
kind: ConfigMap              # 리소스 종류 지정 ConfigMap
metadata:
  name: my-config            # ConfigMap의 이름 (필수)
  namespace: default         # 네임스페이스 (생략 시 default) -  클러스터 내에서 리소스를 논리적으로 분리하여 더 잘 관리하고, 보안, 자원 제한, 협업을 가능하게 해주는 핵심 기능

# 아래의 data 항목은 설정 값을 key-value 형태로 저장
# 각 key는 환경 변수명이나 파일명으로 사용 가능
data:
  # key: 값 형태로 지정
  gateway_ip: "192.168.65.3"
  token_expiration-time: "86400000"
  token_secret: "asd123asdkz9xc8v123klxzc890234kljasd8213sd8k8s"
  order-service-url: "http://order-service:10000"
  bootstrap-servers: "192.168.65.3:9092"
```


## 7 ) Deployment
```yaml
# ✅ 배포를 해주는 묶음으로 생각하자. 
#   - kind: Deployment 를 통해 구조를 잡고
#   - kind: Service를 통해 network 구조를 잡음
```

### 7 - 1 ) selector 구조
- Label과 Selector를 통해 이루어짐 즉, 직접 참조하지 않고, 서로 “라벨로 연결” 함
  - deployment :`template.metadata.labels.app = user-app`
  - service : `selector.app =  user-app`

### 7 - 2 ) 구성 흐름
```text
[Deployment]
    ↓ (생성)
  [Pod] ← labels: app=user-app
    ↑
[Service]
  selector: app=user-app
```

### 7 - 3 ) 예시 ( user-service Deployment )
```yaml
# ----------------------------------------
# 🛠 Deployment: user-deploy
# - user-service 컨테이너를 실행하는 Deployment 정의
# - 환경 변수는 ConfigMap으로부터 주입
# ----------------------------------------
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-deploy
spec:
  selector:
    matchLabels:
      app: user-app         # 이 레이블을 가진 Pod을 선택
  replicas: 1                # Pod 수: 1개
  template:
    metadata:
      labels:
        app: user-app       # Pod에 부여될 레이블
    spec:
      containers:
        - name: user-service
          image: edowon0623/user-service:ssg_v1.1  # 배포할 컨테이너 이미지
          imagePullPolicy: Always                  # 항상 이미지를 pull하여 최신 상태 유지
          ports:
            - containerPort: 60000                 # 컨테이너 내부에서 사용하는 포트
              protocol: TCP
          resources:
            requests:
              cpu: 500m                            # 최소 CPU 리소스 요청 (0.5 Core)
              memory: 1000Mi                       # 최소 메모리 리소스 요청 (1000 MiB)
          env:                                     # 환경 변수 설정 (ConfigMap으로부터 주입)
            - name: GATEWAY_IP
              valueFrom:
                configMapKeyRef:
                  name: msa-k8s-configmap
                  key: gateway_ip
            - name: TOKEN_EXPIRATION_TIME
              valueFrom:
                configMapKeyRef:
                  name: msa-k8s-configmap
                  key: token_expiration_time
            - name: TOKEN_SECRET
              valueFrom:
                configMapKeyRef:
                  name: msa-k8s-configmap
                  key: token_secret
            - name: ORDER-SERVICE-URL
              valueFrom:
                configMapKeyRef:
                  name: msa-k8s-configmap
                  key: order-service-url

---

# ----------------------------------------
# 🌐 Service: user-service [ 네트워크를 설정하는 기능 ]
# - user-service Deployment에 외부 접근을 허용하기 위한 NodePort 서비스 정의
# - 외부에서 NodeIP:30001으로 접근 가능
# ----------------------------------------
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  type: NodePort                      # 외부에서 접근 가능한 타입
  selector:
    app: user-app                     # 해당 레이블을 가진 Pod에 요청 전달
  ports:
    - protocol: TCP
      port: 60000                     # 클러스터 내부에서 사용하는 서비스 포트
      targetPort: 60000              # 컨테이너의 포트
      nodePort: 30001                # 외부에서 접근할 수 있는 노드 포트 [ 포트포워딩과 같은 개념 ]
```

## 8 ) Pod 내 컨테이너에서 ConfigMap 사용

### 8 - 1 ) 설정 적용 방법
- 1 . configmap 기동
- 2 . Deployment 내 `spec.template.spec.containers.env.name`을 배열 형식으로 지정
  - 실질적으로 ConfigMap과 이어주는 역할은 Deploy에서 진행
- 3 . 실제 Image가 될 application 설정 파일 수정 ( user-service 설정 일 부분)
  ```yaml
  order-service-url: ${ORDER-SERVICE-URL}
  
  gateway:
    ip: ${GATEWAY_IP}
  
  token:
    expiration_time: ${TOKEN_EXPIRATION_TIME}
    secret: ${TOKEN_SECRET}
  ```

## 9 ) 전체 흐름
- configMap 기동
- service 별 Deployment 작성
  - service별 application.yml 수정
- service별 image 생성
- Deployment 내 image 지댓
- kubectl apply -f 대상을 통해 리소스 생성