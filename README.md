# MSA Study

## Cloud Native Architecture
- ### 확장 가능한 아키텍처
  - 시스템의 scale-out에 유리함
  - 확장된 서버로 시스템 부하 분산, 가용성 보정
  - 컨테이너 기반 패키지 가능
  - 모니터링
- ### 탄력적 아키텍처
  - CI/CD를 통한 비즈니스 환경 변화에 대응 시간 단축
  - 분활된 서비스 구조
    - 마이크로 서비스들의 존재는 **Discovery Service라는 곳에 등록** 및 삭제되는 작업을 함
      - 다른 서비스들이나 외부에 연결되어있는 타 시스템에서도 해당 서비스를 찾을 수 있게 하기 위함
  - 무상태 통신 통신 프로토콜
  - 서비스의 추가와 삭제가 자동으로 감지
  - 변경된 서비스 요청에 따라 사용자 요청 처리
- ### 장애 격리
  - 특정 서비스만 오류가 발생하도 다른 서비스에 영향을 주지 않음

## MicroService의 특정
- 1 . challenges
  - 여러 서비스 간의 데이터 동기화 문제를 해결하기 위해 이벤트 기반 아키텍처를 도입
- 2 . Small Well Chosen Deployable Unit
  - 사용자 인증 기능만을 담당하는 독립적인 마이크로서비스를 개발하여 배포
- 3 . Bounded Context
  - 주문 처리와 결제 서비스를 별도의 경계로 설정하여 서로 독립적으로 작동하게 합니다.
- 4 . RESTfut
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

## Spring Cloud란?
- 분산 시스템(distributed systems)에서 흔히 발생하는 문제를 해결하기 위한 Spring Framework 기반의 도구 모음
- MSA 구조를 구현하거나 관리할 때 유용한 기능을 제공
- 다양한 클라우드 제공자와 통합될 수 있으며, 개발자가 복잡한 인프라 문제를 처리하지 않고 비즈니스 로직에 집중할 수 있도록 함

### 주의 사항
  - Spring Boot + Spring Cloud 형태이다.
    - 각각의 프레임 워크는 서로의 버전을 맞춰줘야 한다 ( 호환 이슈 )

### 중요 구성 요소
- Centralized Configuration Management
  - 환경 설정 서버 : 다양한 마이크로 서버를 하나의 환경 설정 서버를 사용해서 모두 적용하는 것
    - Spring Cloud Config Server
- Location Transparency
  - 서비스 등록 및 위치 확인 
    - Naming Server ( Eureka )
- Load Distribution ( Load Balancing )
  - 로드 밸런싱
    - Ribbon ( Client Side )
    - Spring Cloud Gateway  [ 👍 최신 Spring Cloud에서 해당 방법 사용 권장 ]
- Easier REST Client
  - FeignClient
- Visibility and Monitoring
  - Zipkin Distributed Tracing 
  - Netflix API gateway
- Fault Tolerance
  - Hystrix
