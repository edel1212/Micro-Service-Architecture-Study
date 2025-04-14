# MSA Study

## 1 ) Cloud Native Architecture
### 1 - 1 ) 확장 가능한 아키텍처
- 시스템의 scale-out에 유리함
- 확장된 서버로 시스템 부하 분산, 가용성 보정
- 컨테이너 기반 패키지 가능
- 모니터링
### 1 - 2 ) 탄력적 아키텍처
- CI/CD를 통한 비즈니스 환경 변화에 대응 시간 단축
- 분활된 서비스 구조
  - 마이크로 서비스들의 존재는 **Discovery Service라는 곳에 등록** 및 삭제되는 작업을 함
    - 다른 서비스들이나 외부에 연결되어있는 타 시스템에서도 해당 서비스를 찾을 수 있게 하기 위함
- 무상태 통신 통신 프로토콜
- 서비스의 추가와 삭제가 자동으로 감지
- 변경된 서비스 요청에 따라 사용자 요청 처리
### 1 - 3 ) 장애 격리
- 특정 서비스만 오류가 발생하도 다른 서비스에 영향을 주지 않음

## 2 ) MicroService의 특정
- 1 . challenges
  - 여러 서비스 간의 데이터 동기화 문제를 해결하기 위해 이벤트 기반 아키텍처를 도입
- 2 . Small Well Chosen Deployable Unit
  - 사용자 인증 기능만을 담당하는 독립적인 마이크로서비스를 개발하여 배포
- 3 . Bounded Context
  - 주문 처리와 결제 서비스를 별도의 경계로 설정하여 서로 독립적으로 작동하게 합니다.
- 4 . Restful
  - 모든 MicroService는 RESTFul API를 통해서만 소통한다.
- 5 . Configuration Management
  -  Config 서버를 사용하여 각 마이크로서비스의 환경 구성을 중앙에서 관리합니다.
- 6 . Cloud Enabled
  - 컨테이너를 통한 배포
- 7 . Dynamic Scale Up And Scale Down
  - 트래픽이 많아지면 오토스케일링을 통해 서비스 인스턴스를 증가시키고, 트래픽 감소 시 자동으로 줄입니다.
- 8 . CI / CD
  - 코드 변경이 발생하면 자동으로 빌드 및 배포
- 9 . Visibility
  - 각 마이크로서비스의 상태와 성능을 실시간으로 모니터링

## 3 ) Spring Cloud란?
- 분산 시스템(distributed systems)에서 흔히 발생하는 문제를 해결하기 위한 Spring Framework 기반의 도구 모음
- MSA 구조를 구현하거나 관리할 때 유용한 기능을 제공
- 다양한 클라우드 제공자와 통합될 수 있으며, 개발자가 복잡한 인프라 문제를 처리하지 않고 비즈니스 로직에 집중할 수 있도록 함
- **Spring Boot + Spring Cloud 형태**
  - 각각의 프레임 워크는 서로의 버전을 맞춰줘야 한다 ( 호환 이슈 )

## 4 ) 구성 요소
- Centralized Configuration Management **( Config Service )**
  - 다양한 마이크로 서버를 하나의 환경 설정 서버를 통해 설정 적용
  - ✅ dependencies : `Spring Cloud Config Server`
- Location Transparency **( Discovery Service )**
  - Service 등록 및 위치 확인
  - ✅ dependencies : `Eureka Server`
- Load Distribution **( Gateway service )**
  - 로드 밸런싱 및 라우팅
   ✅ dependencies : `Spring Cloud Gateway`
- Easier REST Client **( 연계 )**
  - Service 간 연계
  - ✅ dependencies : `Feign Client`
- Visibility and Monitoring
  - 모니터링 및 분산 추적 
  - ✅ dependencies : `Zipkin, Grafana, Prometheus`
- Fault Tolerance  **( 장애 처리 )**
  - 장애 발생 시 대처 방법
  - ✅ dependencies : `Circuit Breaker` 


## 5 ) 구성 요소별 사용 방법

### 5 - 1 ) Discovery Service - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/discovery-service)
- 1 . Discovery Service 란?
- 2 . Flow
- 3 . 설정 방법
- 4 . Discovery Client 설정 및 사용 방법 - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/eureka-client)
- 5 . Discover Service 이중화

### 5 - 2 ) Config Service - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/config-service)
- 1 . Spring Config Server 란?
- 2 . 설정 파일 read 우선 순서
- 3 . Config Server 설정 방법
- 4 . Client에서 Config Service 사용
- 5 . [Config Client] 변경된 config service 값 갱신 방법 ( Actuator refresh 방식 )
- 6 . [Config client] GateWay Service 적용 ( Actuator refresh 빙식 )
- 7 . [Config client] profile별 설정 값 가져오기
- 8 . 대칭 암호화 
- 9 . 비대칭 암호화

### 5 - 3 ) Gateway Service - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/api-gateway)
- 1 . 주요 기능
- 2 . Spring Cloud Gateway 란?
- 3 . 설정 방법
- 4 . Gateway CustomFilter
- 5 . Gateway GlobalFilter 
- 6 . OrderedGatewayFilter - 순서 지정 필터
- 7 . 필터 활용 - cookie 및 middle path 제외 
- 8 . Load Balancing - Eureka 연동 

### 5 - 4 ) Spring Cloud Bus- [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/spring-cloud-bus)
- 1 . AMQP( Advanced Message Queuing Protocol ) 란?
- 2 . 전체 흐름
- 3 . RabbitMQ
- 4 . 설정 및 사용 방법
- 5 . spring cloud bus 사용 흐름

### 5 - 5 ) Kafka - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/kafka)
- 1 . 기본 명령어
- 2 . Kafka Connect 란?
- 3 . Kafka using spring-boot
- 4 . afka Connect using spring-boot
- ✅[추가 참고](https://github.com/edel1212/messageQueueStudy)

### 5 - 6 ) RestTemplate - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/restTemplate)
- 1 . Config Class
- 2 . 사용 예시 코드 - (user-service)

### 5 - 7 ) Feign - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/feign)
- 1 . Feign 란?
- 2 . 설정 방법
- 3 . BusinessLogic 내 Feign 사용
- 4 . Log 설정
- 3 . 예외 처리

### 5 - 8 ) CircuitBreaker - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/circuitBreaker)
- 1 . Resilience4j 란?
- 2 . default 설정 사용 방법
- 3 . Custom Resilience4j Config 사용 방법
- 4 . CircuitBreaker 상태 확인

### 5 - 9 ) Zipkin - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/zipkin)
- 1 . Zipkin Server
- 2 . Spring-Boot Connect Zipkin
- 3 . 흐름

### 5 - 10 ) Monitoring - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/monitoring)
- 1 . Docker Compose

### 5 - 11 ) Docker Compose build MSA Server - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/docker-compose)
- 1 . 전체 디렉토리 구조
- 2 . 진행간 이슈 내용

### 5 - 12 ) k8s build MSA Server - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/k8s)
```yaml
# ✅ 기존 코드에서 변경이 많기에 branch를 추가하여 진행 
```
