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


## 기본 명령어

- Kubernetes 노드 확인
  - `kubeclt get node`
- Kubernetes 지정 노드의 리소스 정보 확인
  - `kubectl describe node docker-desktop`
- 지정 리소스를 생성하거나 존재할 경우 update 함
  `kubectl apply -f configmap.yml`
- configmap 리소스 정보 확인
  - `kubectl get configmap`

## Deployment

- 생성 방식
  -  Label과 Selector를 통해 이루어짐 즉, 직접 참조하지 않고, 서로 “라벨로 연결”됩니다
    - deployment :`template.metadata.labels.app = user-app`
    - service : `selector.app =  user-app`
```text
[Deployment]
    ↓ (생성)
  [Pod] ← labels: app=user-app
    ↑
[Service]
  selector: app=user-app
```
